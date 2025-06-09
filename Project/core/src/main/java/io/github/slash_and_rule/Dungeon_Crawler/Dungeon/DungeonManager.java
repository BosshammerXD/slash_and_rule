package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.ArrayList;
import java.util.Stack;

import com.badlogic.gdx.assets.AssetManager;

import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Interfaces.Initalizable;

public class DungeonManager implements Initalizable {
    private String AssetFolder;
    private ArrayList<DungeonTileMap> rooms;
    private int depth;

    public DungeonManager(BaseScreen screen, String assetFolder, int depth) {
        this.AssetFolder = assetFolder;
        this.depth = depth;

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
    // rooms get more dificult as you go deeper
    // Lootrooms as endpoints (with Loot increasing with depth)
    // arrays with possible rooms per Level (Store the Paths)

    private void getRooms() {
        // This method will return a list of DungeonTileMap objects representing the
        // rooms in the dungeon
        // For now, we can return an empty list or add some placeholder rooms
        this.rooms = new ArrayList<>();
    }

    @Override
    public void init(AssetManager assetManager, Stack<Runnable> todo) {
        todo.push(() -> generateDungeon()); // Example depth, can be adjusted);
        todo.push(() -> getRooms());
    }

    @Override
    public void dispose() {

    }

    @Override
    public void show(AssetManager assetManager) {
        // TODO Auto-generated method stub

    }
}
