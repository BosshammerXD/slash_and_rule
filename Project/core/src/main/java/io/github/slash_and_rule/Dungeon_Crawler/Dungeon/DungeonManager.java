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
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.Player;
import io.github.slash_and_rule.Interfaces.Displayable;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.LoadingScreen.LoadingSchedule;
import io.github.slash_and_rule.LoadingScreen.ThreadData;
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

        this.room = new RoomDataHandler(screen, this::changeRoom);
        this.neighbours = new RoomDataHandler[] {
                new RoomDataHandler(screen, this::changeRoom),
                new RoomDataHandler(screen, this::changeRoom),
                new RoomDataHandler(screen, this::changeRoom),
                new RoomDataHandler(screen, this::changeRoom)
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
    public void init(LoadingSchedule loader) {
        this.renderer = new OrthogonalTiledMapRenderer(null, 1 / 16f);
        BitSet roomStructure = new BitSet(((depth + branchcap) * 2 + 1) * ((depth + branchcap) * 2 - 1));
        dungeon = new DungeonRoom(depth, maxDifficulty, roomStructure,
                random, branchcap, branchmul);

        loader.threads.add(new ThreadData(dungeon, this::loadRooms));
        // loader.threads.add(new ThreadData(() -> this.setRendererWhenReady()));
    }

    private void loadRooms() {
        loadRoom(this.room, dungeon, true, true, () -> this.renderer.setMap(this.room.map));

        int count = 0;
        for (DungeonRoom neighbour : dungeon.neighbours) {
            if (neighbour != null) {
                loadRoom(this.neighbours[count], neighbour);
            } else {
                this.neighbours[count].clear();
            }
            count++;
        }
    }

    private boolean processing = false;

    private void loadRooms(int originDir) {
        if (processing) {
            System.out.println("Already processing room change, ignoring request.");
            return; // Prevent re-entrance
        }
        processing = true; // Set processing flag
        if (originDir < 0 || originDir >= 4) {
            throw new IllegalArgumentException("Invalid origin direction: " + originDir);
        }

        this.screen.schedule.add(() -> {
            this.room.setActive(false);
            RoomDataHandler holder = this.neighbours[(originDir + 2) % 4];
            this.neighbours[(originDir + 2) % 4] = this.room;
            this.room = this.neighbours[originDir];
            this.neighbours[originDir] = holder;
            this.dungeon = this.dungeon.neighbours[originDir];
            this.renderer.setMap(this.room.map);
            float[] spawnPos = this.room.doors[(originDir + 2) % 4].getSpawnPos();
            player.setPosition(spawnPos[0], spawnPos[1]);
            this.room.setActive(true);
            this.room.setOpen(true);
            loadRoom(this.neighbours[originDir], dungeon.neighbours[originDir]);
            loadRoom(this.neighbours[(originDir + 1) % 4], dungeon.neighbours[(originDir + 1) % 4]);
            loadRoom(this.neighbours[(originDir + 3) % 4], dungeon.neighbours[(originDir + 3) % 4]);
            processing = false; // Reset processing flag
        });
    }

    private void getData(RoomDataHandler handler, DungeonRoom room, Consumer<RoomData> callback) {
        if (room == null) {
            return; // No room to load
        }
        if (room.path == null) {
            room.path = getPath(room.difficulty, room.type);
        }
        RoomData data = roomCache.get(room.path);
        if (data == null) {
            roomCache.put(room.path, new RoomData(screen, room.path, callback));
        } else {
            data.addCallback(callback);
        }
    }

    private void loadRoom(RoomDataHandler handler, DungeonRoom room) {
        handler.clear();
        getData(handler, room, newData -> {
            handler.loadRoomData(newData, room);
        });
    }

    private void loadRoom(RoomDataHandler handler, DungeonRoom room, boolean isActive, boolean isOpen,
            Runnable onLoad) {
        handler.clear();
        getData(handler, room, newData -> {
            handler.loadRoomData(newData, room, isOpen, isActive);
            onLoad.run();
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
        System.out.println("Changing room in direction: " + direction);
        loadRooms(direction);
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

    @Override
    public void hide() {
    }
}
