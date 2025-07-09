package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Ashley.Builder.WeaponBuilder;
import io.github.slash_and_rule.Ashley.Systems.DungeonSystem;
import io.github.slash_and_rule.Ashley.Systems.EnemySystem;
import io.github.slash_and_rule.Ashley.Systems.HealthSystem;
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
    private WeaponBuilder weaponBuilder;

    public DungeonCrawlerScene(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager, true);

        this.weaponBuilder = new WeaponBuilder(physicsBuilder, engine);

        this.dungeonData = new DungeonData(physicsBuilder, weaponBuilder, entityManager, atlasManager, assetManager);

        this.viewport = new ExtendViewport(16, 9, camera);

        engine.addSystem(new WeaponSystem(Globals.WeaponSystemPriority));

        // Add player and other game objects here
        player = new Player(physicsBuilder, weaponBuilder, camera, entityManager);
        dungeonManager = new DungeonManager(this, new DungeonGenerationData(3, 6, 1, 0.5f), 1 / 32f);
        dungeonSystem = new DungeonSystem(Globals.DungeonSystemPriority, dungeonManager, physicsBuilder, camera,
                1 / 32f);

        System.out.println(Gdx.input.getInputProcessor());
    }

    @Override
    public void init(LoadingScreen loader) {
        addToEngine(loader, dungeonSystem);
        addToEngine(loader, new EnemySystem(world, Globals.EnemySystemPriority));
        addToEngine(loader, new HealthSystem(Globals.HealthSystemPriority));
        loader.schedule("loading level", () -> {
            dungeonManager.level = dungeonData.load(Globals.level);
            dungeonManager.init(loader);
        });
        loader.schedule("loading player", () -> {
            player.init();
        });
        super.init(loader);
    }

    @Override
    public void hide() {
        super.hide();
        dungeonManager.dispose();
    }
}
