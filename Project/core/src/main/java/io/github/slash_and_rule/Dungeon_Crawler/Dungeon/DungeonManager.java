package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import com.badlogic.gdx.assets.AssetManager;

import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.LoadingScreen.LoadingSchedule;
import io.github.slash_and_rule.LoadingScreen.ThreadData;

public class DungeonManager implements Initalizable {
    public static class LevelData {
        public String startRoom;
        public String[] fillerRooms;
        public String[] leafRooms;
        public String endRoom;

        public LevelData(String location, String startRoom, String[] fillerRooms, String[] leafRooms, String endRoom) {
            this.startRoom = location + startRoom;
            this.fillerRooms = Arrays.stream(fillerRooms)
                    .map(room -> location + room)
                    .toArray(String[]::new);
            this.leafRooms = Arrays.stream(leafRooms)
                    .map(room -> location + room)
                    .toArray(String[]::new);
            this.endRoom = location + endRoom;

        }
    }

    private String AssetFolder;
    private ArrayList<DungeonTileMap> rooms;
    private int depth;
    private int branchcap;
    private float branchmul;
    private int maxDifficulty;
    private DungeonRoom dungeon;

    private LevelData[] levels;

    public DungeonManager(BaseScreen screen, String assetFolder, int depth, int maxDifficulty, int branchcap,
            float branchmul) {
        this.AssetFolder = assetFolder;
        this.depth = depth;
        this.branchcap = branchcap;
        this.branchmul = branchmul;
        this.maxDifficulty = maxDifficulty;
        this.levels = new LevelData[] {
                new LevelData(assetFolder + "/testlevel", "start", new String[] { "filler" }, new String[] { "leaf" },
                        "end"), };

        screen.loadableObjects.add(this);
    }

    public void generateDungeon() {
        // This method will contain the logic to generate a dungeon layout
        // It will create rooms, corridors, and other features of the dungeon
        // The implementation details will depend on the specific requirements of the
        // game
        // For now, we can leave it empty or add a simple placeholder implementation
    }

    // Split up dungeon into Levels
    // Level x has Boss at end
    // rooms get more dificult as you go deeper [done]
    // Lootrooms as endpoints (with Loot increasing with depth) [done]
    // arrays with possible rooms per Level (Store the Paths)

    private void getRooms() {
        // This method will return a list of DungeonTileMap objects representing the
        // rooms in the dungeon
        // For now, we can return an empty list or add some placeholder rooms
        this.rooms = new ArrayList<>();
    }

    @Override
    public void init(LoadingSchedule loader) {

        BitSet roomStructure = new BitSet(((depth + branchcap) * 2 + 1) * ((depth + branchcap) * 2 - 1));
        dungeon = new DungeonRoom(depth, maxDifficulty, roomStructure,
                new Random(), branchcap, branchmul);

        loader.threads.add(new ThreadData(dungeon));
    }

    @Override
    public void dispose() {

    }

    @Override
    public void show(AssetManager assetManager) {
        // TODO Auto-generated method stub
        dungeon.print();
        System.out.println("Dungeon generated with " + DungeonRoom.numRooms + " rooms.");
    }
}
