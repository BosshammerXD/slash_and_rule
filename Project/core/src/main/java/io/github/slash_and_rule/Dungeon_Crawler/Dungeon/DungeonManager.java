package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Consumer;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;

import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.Player;
import io.github.slash_and_rule.Interfaces.Displayable;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.LoadingScreen.ThreadData;
import io.github.slash_and_rule.Utils.Generation;
import io.github.slash_and_rule.Utils.LRUCache;

public class DungeonManager implements Initalizable, Disposable, Displayable {
    public static class LevelData {
        public String startRoom;
        public String[] fillerRooms;
        public String[] leafRooms;
        public String endRoom;

        public LevelData(String location, String startRoom, String[] fillerRooms, String[] leafRooms, String endRoom) {
            this.startRoom = location + startRoom + ".tmx";
            this.fillerRooms = Arrays.stream(fillerRooms)
                    .map(room -> location + room + ".tmx")
                    .toArray(String[]::new);
            this.leafRooms = Arrays.stream(leafRooms)
                    .map(room -> location + room + ".tmx")
                    .toArray(String[]::new);
            this.endRoom = location + endRoom + ".tmx";

        }
    }

    private LRUCache<String, RoomData> roomCache = new LRUCache<>(10);

    public int currentLevel;
    private int depth;
    private int branchcap;
    private float branchmul;
    private int maxDifficulty;
    private DungeonRoom dungeon;
    private Player player;

    private PhysicsScreen screen;

    private RoomDataHandler room;
    private RoomDataHandler[] neighbours = new RoomDataHandler[4]; // left, right, top, bottom

    private LevelData[] levels;

    private Random random = new Random();
    private OrthogonalTiledMapRenderer renderer;

    private Generation loadGeneration = new Generation();

    public DungeonManager(PhysicsScreen screen, InputManager inputManager,
            Player player,
            String assetFolder,
            int depth, int maxDifficulty,
            int branchcap,
            float branchmul, int currentLevel) {

        this.currentLevel = currentLevel;
        this.depth = depth;
        this.branchcap = branchcap;
        this.branchmul = branchmul;
        this.maxDifficulty = maxDifficulty;
        this.player = player;

        this.room = new RoomDataHandler(screen, this::changeRoom, loadGeneration);
        this.neighbours = new RoomDataHandler[] {
                new RoomDataHandler(screen, this::changeRoom, loadGeneration),
                new RoomDataHandler(screen, this::changeRoom, loadGeneration),
                new RoomDataHandler(screen, this::changeRoom, loadGeneration),
                new RoomDataHandler(screen, this::changeRoom, loadGeneration)
        }; // left, right, top, bottom

        this.screen = screen;

        this.levels = new LevelData[] {
                new LevelData(assetFolder + "/testlevel/", "start", new String[] { "filler" }, new String[] { "leaf" },
                        "end"), };

        RoomData.scale = 1 / 16f;

        screen.loadableObjects.add(this);
        screen.disposableObjects.add(this);
        screen.drawableObjects.add(this);
    }

    // Split up dungeon into Levels
    // Level x has Boss at end
    // rooms get more dificult as you go deeper [done]
    // Lootrooms as endpoints (with Loot increasing with depth) [done]
    // arrays with possible rooms per Level (Store the Paths)

    @Override
    public void init(LoadingScreen loader) {
        this.renderer = new OrthogonalTiledMapRenderer(null, 1 / 16f);
        BitSet roomStructure = new BitSet(((depth + branchcap) * 2 + 1) * ((depth + branchcap) * 2 - 1));
        dungeon = new DungeonRoom(depth, maxDifficulty, roomStructure,
                random, branchcap, branchmul);
        loader.threads.add(new ThreadData(dungeon, () -> loadRooms(loader)));
        // loader.threads.add(new ThreadData(() -> this.setRendererWhenReady()));
    }

    private void loadRooms(LoadingScreen loader) {
        loadFirstRoom(this.room, dungeon, loader, () -> this.renderer.setMap(this.room.map));

        int count = 0;
        for (DungeonRoom neighbour : dungeon.neighbours) {
            if (neighbour != null) {
                loadRoomInit(this.neighbours[count], neighbour, loader);
            } else {
                this.neighbours[count].clear();
            }
            count++;
        }
    }

    private boolean processing = false;

    private void loadRooms(int originDir, int generation) {
        if (processing) {
            System.out.println("Already processing room change, ignoring request.");
            return; // Prevent re-entrance
        }
        processing = true; // Set processing flag
        if (originDir < 0 || originDir >= 4) {
            throw new IllegalArgumentException("Invalid origin direction: " + originDir);
        }

        this.screen.schedule_generation(() -> {
            this.room.setActive(false); // Deactivate current room

            // Shift the sStructure so that the room you wanted to go to is now the current
            // room
            // and the current room is now the neighbour in the direction you came from
            RoomDataHandler holder = this.neighbours[(originDir + 2) % 4];
            this.neighbours[(originDir + 2) % 4] = this.room;
            this.room = this.neighbours[originDir];
            // put the room that was in the ddirection you cam from where the room
            // you moved to was (so we don't have the same room obj more than once)
            this.neighbours[originDir] = holder;
            this.dungeon = this.dungeon.neighbours[originDir]; // Update dungeon reference
            this.renderer.setMap(this.room.map); // Update renderer map
            this.screen.halt = true;
            this.room.addCallback(handler -> {
                float[] spawnPos = handler.doors[(originDir + 2) % 4].getSpawnPos();
                handler.setActive(true);
                handler.setOpen(true);

                this.neighbours[originDir].clear();
                this.neighbours[(originDir + 1) % 4].clear();
                this.neighbours[(originDir + 3) % 4].clear();

                this.room.setNeighbours(this.neighbours, generation);
                /// layer.setPosition(spawnPos[0], spawnPos[1]);
                this.screen.halt = false; // Resume screen processing

                loadRoom(this.neighbours[originDir], dungeon.neighbours[originDir],
                        generation);
                loadRoom(this.neighbours[(originDir + 1) % 4], dungeon.neighbours[(originDir + 1) % 4],
                        generation);
                loadRoom(this.neighbours[(originDir + 3) % 4], dungeon.neighbours[(originDir + 3) % 4],
                        generation);
                processing = false; // Reset processing flag
            }, generation);
        }, this.loadGeneration, generation);
    }

    private void getData(RoomDataHandler handler, DungeonRoom room, BaseScreen myScreen, Consumer<RoomData> callback) {
        if (room == null) {
            return; // No room to load
        }
        if (room.path == null) {
            room.path = getPath(room.difficulty, room.type);
        }
        RoomData data = roomCache.get(room.path);
        if (data == null) {
            roomCache.put(room.path, new RoomData(myScreen, room.path, callback));
        } else {
            data.addCallback(callback);
        }
    }

    private void loadRoom(RoomDataHandler handler, DungeonRoom room, int generation) {
        getData(handler, room, screen, newData -> {
            if (loadGeneration.get() != generation) {
                return; // Ignore if generation has changed
            }
            handler.loadRoomData(newData, room);
        });
    }

    private void loadFirstRoom(RoomDataHandler handler, DungeonRoom room, LoadingScreen loader,
            Runnable onLoad) {
        handler.clear();
        getData(handler, room, loader, newData -> {
            handler.loadRoomData(newData, room, true, true, this.neighbours);
            handler.addCallback(myData -> this.renderer.setMap(myData.map), 0);
        });
    }

    private void loadRoomInit(RoomDataHandler handler, DungeonRoom room, LoadingScreen loader) {
        handler.clear();
        getData(handler, room, loader, newData -> {
            handler.loadRoomData(newData, room);
        });
    }

    private String getPath(int difficulty, byte type) {
        if (type == 0) {
            return levels[currentLevel].startRoom; // Placeholder for start room
        } else if (type == 3) {
            return levels[currentLevel].endRoom; // Placeholder for end room
        }

        return (type == 1) ? pickRandom(levels[currentLevel].fillerRooms) : pickRandom(levels[currentLevel].leafRooms); // Placeholder
    }

    private String pickRandom(String[] array) {
        if (array.length == 0) {
            return null; // No rooms available
        }
        return array[random.nextInt(array.length)];
    }

    private void changeRoom(Integer direction) {
        loadGeneration.inc();
        final int myGeneration = loadGeneration.get(); // Capture current generation
        // this.screen.schedule.clear();
        // this.screen.processingQueue.clear();
        loadRooms(direction, myGeneration);
    }

    @Override
    public void dispose() {
        this.renderer.dispose();
        for (RoomDataHandler neighbour : neighbours) {
            neighbour.dispose();
        }
        room.dispose();
        for (RoomData room : roomCache.values()) {
            room.dispose();
        }
        roomCache.clear();
    }

    @Override
    public void show(AssetManager assetManager) {
        // TODO Auto-generated method stub
        dungeon.print();
        System.out.println("Dungeon generated with " + DungeonRoom.numRooms + " rooms.");
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (renderer.getMap() != null) {
            renderer.setView(screen.camera);
            renderer.render();
        }
    }
}
