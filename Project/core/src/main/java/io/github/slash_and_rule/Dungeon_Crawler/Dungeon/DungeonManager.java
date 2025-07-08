package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.BitSet;
import java.util.function.Consumer;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Disposable;

import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.DungeonData.LevelData;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.LoadingScreen.ThreadData;
import io.github.slash_and_rule.Utils.LRUCache;

public class DungeonManager implements Initalizable, Disposable {
    public static class DungeonGenerationData {
        public int depth;
        public int maxDifficulty;
        public int branchcap;
        public float branchmul;
        private BitSet roomStructure;
        private int arrayLength;

        public DungeonGenerationData(int depth, int maxDifficulty, int branchcap, float branchmul) {
            this.depth = depth;
            this.maxDifficulty = maxDifficulty;
            this.branchcap = branchcap;
            this.branchmul = branchmul;
        }

        public void genRoomStructure() {
            this.roomStructure = new BitSet(((depth + branchcap) * 2 + 1) * ((depth + branchcap) * 2));
            this.arrayLength = (depth + branchcap) * 2 + 1;
        }

        public BitSet getRoomStructure() {
            return this.roomStructure;
        }

        public int getArrayLength() {
            return this.arrayLength;
        }
    }

    private LRUCache<String, RoomData> roomCache = new LRUCache<>(10);

    private DungeonGenerationData generationData;
    private DungeonRoom dungeon;

    private PhysicsScreen screen;

    public LevelData level;

    private Consumer<LoadingScreen> onDungeonGenerated;

    public DungeonManager(PhysicsScreen screen, DungeonGenerationData generationData, float scale) {

        this.generationData = generationData;

        this.screen = screen;

        RoomData.scale = scale;

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
        generationData.genRoomStructure();
        dungeon = new DungeonRoom(generationData);
        loader.threads.add(new ThreadData(dungeon, () -> {
            System.out.println("Dungeon generated, loading rooms...");
            onDungeonGenerated.accept(loader);
            dungeon.print();
            System.out.println("Dungeon generated with " + DungeonRoom.numRooms + " rooms.");
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
            roomCache.put(room.path, new RoomData(myScreen, room.path, myScreen.getAssetManager(), callback));
        } else {
            data.addCallback(callback);
        }
    }

    private String getPath(int difficulty, byte type) {
        if (type == 0) {
            return level.startRoom; // Placeholder for start room
        } else if (type == 3) {
            return level.endRoom; // Placeholder for end room
        }
        if (type == 1) {
            return level.fillerRoomsCollection.next();
        } else {
            return level.leafRoomsCollection.next();
        }
    }

    @Override
    public void dispose() {
        roomCache.clear();
        dungeon = null;
        DungeonRoom.numRooms = 0;
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
