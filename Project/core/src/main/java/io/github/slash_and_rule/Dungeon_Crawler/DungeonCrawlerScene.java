package io.github.slash_and_rule.Dungeon_Crawler;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Ashley.Systems.DungeonSystem;
import io.github.slash_and_rule.Ashley.Systems.WeaponSystem;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Globals;

public class DungeonCrawlerScene extends PhysicsScreen {
    private Player player;
    private DungeonManager dungeonManager;
    private DungeonSystem dungeonSystem;
    private DungeonData dungeonData;

    public DungeonCrawlerScene(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager, true);
        this.dungeonData = new DungeonData(getPhysicsBuilder(), entityManager, atlasManager, assetManager);
        atlasManager.add("levels/level_1/levelSprites.atlas");

        this.viewport = new ExtendViewport(16, 9, camera);

        engine.addSystem(new WeaponSystem(Globals.WeaponSystemPriority));

        // Add player and other game objects here
        player = new Player(getPhysicsBuilder(), camera, atlasManager, entityManager);
        System.out.println("Player created: " + player);
        dungeonManager = new DungeonManager(this, "levels", 6, 16, 1, 3f, 1);
        dungeonSystem = new DungeonSystem(Globals.DungeonSystemPriority, dungeonManager, getPhysicsBuilder(), camera,
                1 / 32f);

        engine.addSystem(dungeonSystem);

        System.out.println(Gdx.input.getInputProcessor());
    }

    @Override
    public void hide() {
        super.hide();
        player.dispose();
        dungeonManager.dispose();
    }
}
