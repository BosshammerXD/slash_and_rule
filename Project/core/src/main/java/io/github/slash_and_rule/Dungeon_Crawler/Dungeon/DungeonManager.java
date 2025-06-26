package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Consumer;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Disposable;

import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.LoadingScreen.ThreadData;
import io.github.slash_and_rule.Utils.LRUCache;

public class DungeonManager implements Initalizable, Disposable {
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

    private PhysicsScreen screen;

    private LevelData[] levels;

    private Random random = new Random();

    private Consumer<LoadingScreen> onDungeonGenerated;

    public DungeonManager(PhysicsScreen screen,
            String assetFolder,
            int depth, int maxDifficulty,
            int branchcap,
            float branchmul, int currentLevel) {

        this.currentLevel = currentLevel;
        this.depth = depth;
        this.branchcap = branchcap;
        this.branchmul = branchmul;
        this.maxDifficulty = maxDifficulty;

        this.screen = screen;

        this.levels = new LevelData[] {
                new LevelData(assetFolder + "/testlevel/", "start", new String[] { "filler" }, new String[] { "leaf" },
                        "end"), };

        RoomData.scale = 1 / 16f;

        screen.loadableObjects.add(this);
        screen.disposableObjects.add(this);
    }

    public void setOnDungeonGenerated(Consumer<LoadingScreen> onDungeonGenerated) {
        this.onDungeonGenerated = onDungeonGenerated;
    }

    // Split up dungeon into Levels
    // Level x has Boss at end
    // rooms get more dificult as you go deeper [done]
    // Lootrooms as endpoints (with Loot increasing with depth) [done]
    // arrays with possible rooms per Level (Store the Paths)

    @Override
    public void init(LoadingScreen loader) {
        System.out
                .println("DungeonManager: Initializing Dungeon with depth " + depth + ", maxDifficulty " + maxDifficulty
                        + ", branchcap " + branchcap + ", branchmul " + branchmul);
        BitSet roomStructure = new BitSet(((depth + branchcap) * 2 + 1) * ((depth + branchcap) * 2 - 1));
        dungeon = new DungeonRoom(depth, maxDifficulty, roomStructure,
                random, branchcap, branchmul);
        loader.threads.add(new ThreadData(dungeon, () -> {
            System.out.println("Dungeon generated, loading rooms...");
            onDungeonGenerated.accept(loader);
        }));
    }

    public void getData(DungeonRoom room, Consumer<RoomData> callback) {
        getData(room, this.screen, callback);
    }

    public void getData(DungeonRoom room, BaseScreen myScreen, Consumer<RoomData> callback) {
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

    @Override
    public void dispose() {
        for (RoomData room : roomCache.values()) {
            room.dispose();
        }
        roomCache.clear();
    }

    @Override
    public void show(AssetManager assetManager) {
        // TODO
        dungeon.print();
        System.out.println("Dungeon generated with " + DungeonRoom.numRooms + " rooms.");
    }

    public void move(int direction) {
        dungeon = dungeon.neighbours[direction];
    }

    public DungeonRoom getRoom() {
        return dungeon;
    }

    public DungeonRoom[] getNeighbours() {
        return dungeon.neighbours;
    }
}
