package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonTileMap;

public class DungeonCrawlerScene extends PhysicsScreen {

    public DungeonCrawlerScene(AssetManager assetManager) {
        super(true);

        this.viewport = new ExtendViewport(16, 9, camera);

        // Add player and other game objects here
        new Player(this, inputManager, world);
        new DungeonTileMap(this, inputManager, world, "test.tmx");
        new DungeonManager(this, "", 6, 16, 1, 3f);
    }

    @Override
    public void show() {
        // Initialize the scene, load assets, etc.
    }

}
