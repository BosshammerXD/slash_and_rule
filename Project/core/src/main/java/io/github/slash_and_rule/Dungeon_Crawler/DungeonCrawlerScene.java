package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;
import io.github.slash_and_rule.Utils.AtlasManager;

public class DungeonCrawlerScene extends PhysicsScreen {
    private Player player;
    private DungeonManager dungeonManager;

    public DungeonCrawlerScene(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager, true);

        this.viewport = new ExtendViewport(16, 9, camera);

        // Add player and other game objects here
        player = new Player(this, inputManager);
        dungeonManager = new DungeonManager(this, inputManager, player, "levels", 6, 16, 1, 3f, 0);
    }

    @Override
    public void hide() {
        super.hide();
        player.dispose();
        dungeonManager.dispose();
    }
}
