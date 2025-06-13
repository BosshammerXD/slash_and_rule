package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;

public class DungeonCrawlerScene extends PhysicsScreen {

    public DungeonCrawlerScene(AssetManager assetManager) {
        super(true);

        this.viewport = new ExtendViewport(16, 9, camera);

        // Add player and other game objects here
        Player player = new Player(this, inputManager);
        new DungeonManager(this, inputManager, player, "levels", 6, 16, 1, 3f, 0);
    }
}
