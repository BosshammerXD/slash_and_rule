package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.PhysicsScreen;

public class DungeonCrawlerScene extends PhysicsScreen {

    public DungeonCrawlerScene() {
        super(true);

        this.viewport = new ExtendViewport(16, 9, camera);

        // Add player and other game objects here
        new Player(this, inputManager, world);
    }

    @Override
    public void init() {
        // Initialize game objects, load assets, etc.
        // This method can be used to set up the initial state of the scene.
    }

    @Override
    public void show() {
        // Initialize the scene, load assets, etc.
    }

}
