package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Ashley.Systems.DungeonSystem;
import io.github.slash_and_rule.Ashley.Systems.WeaponSystem;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager.DungeonGenerationData;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.LoadingScreen;

public class DungeonCrawlerScene extends PhysicsScreen {
    private Player player;
    private DungeonManager dungeonManager;
    private DungeonSystem dungeonSystem;
    private DungeonData dungeonData;

    public DungeonCrawlerScene(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager, true);
        this.dungeonData = new DungeonData(getPhysicsBuilder(), entityManager, atlasManager, assetManager);

        this.viewport = new ExtendViewport(16, 9, camera);

        engine.addSystem(new WeaponSystem(Globals.WeaponSystemPriority));

        // Add player and other game objects here
        player = new Player(getPhysicsBuilder(), camera, atlasManager, entityManager);
        dungeonManager = new DungeonManager(this, new DungeonGenerationData(6, 16, 1, 3f), 1 / 32f);
        dungeonSystem = new DungeonSystem(Globals.DungeonSystemPriority, dungeonManager, physicsBuilder, camera,
                1 / 32f);

        System.out.println(Gdx.input.getInputProcessor());
    }

    @Override
    public void init(LoadingScreen loader) {
        addToEngine(loader, dungeonSystem);
        loader.schedule("loading level", () -> {
            dungeonManager.level = dungeonData.load(Globals.level);
            dungeonManager.init(loader);
        });
        super.init(loader);
    }

    @Override
    public void hide() {
        super.hide();
        player.dispose();
        dungeonManager.dispose();
    }
}
