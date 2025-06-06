package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.PhysicsScreen;

public class DungeonCrawlerScene extends PhysicsScreen {

    public DungeonCrawlerScene() {
        // Initialize the Box2D world and debug renderer
        super(true);

        // Add player and other game objects here
        new Player(this, inputManager, world);

        this.viewport = new ExtendViewport(16, 9, camera);
    }

    @Override
    public void show() {
        // Initialize the scene, load assets, etc.
    }

}
