package io.github.slash_and_rule.CityBuilder;

import java.util.HashMap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Ressources;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Builder.RenderBuilder;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.CityComponents.BuildingComponent;
import io.github.slash_and_rule.Ashley.Components.CityComponents.BuyableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.ForegroundComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Systems.CitySystems.BuildingShop;
import io.github.slash_and_rule.Ashley.Systems.CitySystems.BuildingSystem;
import io.github.slash_and_rule.Ashley.Systems.CitySystems.CityBGSystem;
import io.github.slash_and_rule.Ashley.Systems.CitySystems.ResourceSystem;
import io.github.slash_and_rule.Bases.GameScreen;
import io.github.slash_and_rule.Dungeon_Crawler.DungeonCrawlerScene;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.Mappers;

public class CityBuilderScene extends GameScreen {
    public DungeonCrawlerScene dungeonCrawlerScene;
    private CityBGSystem cityBGSystem;
    private RenderBuilder<ForegroundComponent> renderBuilder;
    private RenderBuilder<MidfieldComponent> renderBuilder2;

    public CityBuilderScene(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager);
        renderBuilder = new RenderBuilder<ForegroundComponent>();
        renderBuilder2 = new RenderBuilder<MidfieldComponent>();

        Entity entity = new Entity();
        renderBuilder2.begin(new MidfieldComponent());
        renderBuilder2.add("city/Buildings.atlas", "Castle", 0, 1, 1, 0, 0);
        renderBuilder2.end(entity);

        BuildingComponent buildingComponent = new BuildingComponent();
        buildingComponent.name = "Castle";

        EntityManager.build(entity, buildingComponent, new TransformComponent());
        CityData.buildings.add(entity);
    }

    @Override
    public void init(LoadingScreen loader) {
        Ressources.DungeonLeft();
        super.init(loader);
        loader.schedule(() -> {
            assetManager.load("city/Grass.png", Texture.class);
        });
        atlasManager.add("city/Buildings.atlas");
        atlasManager.add("ressources/ressources.atlas");
        addToEngine(loader, cityBGSystem = new CityBGSystem(gameCamera, 11));
        addToEngine(loader, new BuildingShop(uiCamera, gameCamera, atlasManager, 120));
        addToEngine(loader, new BuildingSystem(gameCamera, () -> {
            this.switchScreen = dungeonCrawlerScene;
        }, 67));
        addToEngine(loader, new ResourceSystem(uiCamera, atlasManager, 130));
        loader.schedule(() -> {
            for (Entity entity : CityData.buildings) {
                MidfieldComponent midComp = Mappers.midfieldMapper.get(entity);
                for (MidfieldComponent.TextureData data : midComp.textures) {
                    data.texture = null;
                }

                engine.addEntity(entity);
            }
        });

        loader.schedule(() -> {
            HashMap<String, Integer> cost = new HashMap<>();
            cost.put(Ressources.COIN, 2);
            cost.put(Ressources.SLIME_GEM, 5);
            addBuildingSell("SpearBuilding", cost);
        });
    }

    @Override
    public void show() {
        super.show();
        cityBGSystem.load(assetManager);
        Entity player = engine.createEntity();
        EntityManager.build(player, new PlayerComponent(), new ControllableComponent());
        engine.addEntity(player);
    }

    @Override
    protected void step(float delta) {
    }

    private void addBuildingSell(String name, HashMap<String, Integer> cost) {
        Entity entity = new Entity();
        renderBuilder.begin(new ForegroundComponent());
        renderBuilder.add("city/Buildings.atlas", name, 0, 1, 1, 0, 0);
        renderBuilder.end(entity);

        BuildingComponent buildComp = new BuildingComponent();
        buildComp.name = name;

        BuyableComponent buyableComp = new BuyableComponent();
        buyableComp.cost = cost;

        EntityManager.build(entity, buildComp, buyableComp);
        engine.addEntity(entity);
    }
}
