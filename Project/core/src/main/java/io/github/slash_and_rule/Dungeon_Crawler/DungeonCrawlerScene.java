package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.PhysicsScreen;

public class DungeonCrawlerScene extends PhysicsScreen {

    public DungeonCrawlerScene(AssetManager assetManager) {
        super(true);

        this.viewport = new ExtendViewport(16, 9, camera);

        // Add player and other game objects here
        new Player(this, inputManager, world);
        new Test(this, inputManager, world);
    }

    @Override
    public void show() {
        // Initialize the scene, load assets, etc.
    }

}
