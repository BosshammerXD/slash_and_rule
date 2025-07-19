package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.assets.AssetManager;

import io.github.slash_and_rule.Ashley.Builder.PhysCompBuilder;
import io.github.slash_and_rule.Ashley.Builder.WeaponBuilder;
import io.github.slash_and_rule.Ashley.Systems.DungeonSystems.DoorSystem;
import io.github.slash_and_rule.Ashley.Systems.DungeonSystems.DungeonRoomSystem;
import io.github.slash_and_rule.Ashley.Systems.DungeonSystems.EntrySystem;
import io.github.slash_and_rule.Ashley.Systems.DungeonSystems.HealthSystem;
import io.github.slash_and_rule.Ashley.Systems.DungeonSystems.HealthbarSystem;
import io.github.slash_and_rule.Ashley.Systems.DungeonSystems.ItemSystem;
import io.github.slash_and_rule.Ashley.Systems.DungeonSystems.PlayerSystem;
import io.github.slash_and_rule.Ashley.Systems.DungeonSystems.WeaponSystem;
import io.github.slash_and_rule.Ashley.Systems.EnemySystems.EnemySystem;
import io.github.slash_and_rule.Ashley.Systems.EnemySystems.JumperSystem;
import io.github.slash_and_rule.Bases.GameScreen;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager.DungeonGenerationData;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.LoadingScreen;

public class DungeonCrawlerScene extends PhysicsScreen {
    private Player player;
    private DungeonManager dungeonManager;
    private DungeonData dungeonData;
    private WeaponBuilder weaponBuilder;
    private PhysCompBuilder physCompBuilder;
    private DungeonRoomSystem dungeonRoomSystem;
    public GameScreen cityBuild;

    public DungeonCrawlerScene(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager, false);

        this.physCompBuilder = new PhysCompBuilder(physicsBuilder);

        this.weaponBuilder = new WeaponBuilder(physicsBuilder, engine);

        this.dungeonData = new DungeonData(physicsBuilder, weaponBuilder, entityManager, atlasManager, assetManager);

        // Add player and other game objects here
        player = new Player(physCompBuilder, weaponBuilder);
        dungeonManager = new DungeonManager(this, new DungeonGenerationData(3, 6, 1, 0.5f), 1 / 32f);
    }

    @Override
    public void init(LoadingScreen loader) {
        super.init(loader);
        addToEngine(loader, new EnemySystem(world, physCompBuilder));
        addToEngine(loader, new HealthSystem());
        addToEngine(loader, new WeaponSystem());
        addToEngine(loader, dungeonRoomSystem = new DungeonRoomSystem(physCompBuilder, dungeonManager, gameCamera));
        addToEngine(loader, new DoorSystem(dungeonManager::move));
        addToEngine(loader, new JumperSystem());
        addToEngine(loader, new HealthbarSystem(gameCamera));
        addToEngine(loader,
                new EntrySystem(textCamera, textViewport, gameCamera, () -> {
                    this.switchScreen = cityBuild;
                }));
        addToEngine(loader, new PlayerSystem(gameCamera, () -> {
            this.switchScreen = cityBuild;
        }));
        addToEngine(loader, new ItemSystem());
        loader.schedule("loading player", () -> {
            engine.addEntity(player.init());
        });
        loader.schedule("loading level", () -> {
            dungeonManager.setOnDungeonGenerated(dungeonRoomSystem::init);
            dungeonManager.level = dungeonData.load(Globals.Dungeon.Level);
            dungeonManager.init(loader);
        });
    }

    @Override
    public void hide() {
        super.hide();
        dungeonManager.dispose();
    }
}
