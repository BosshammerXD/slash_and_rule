package io.github.slash_and_rule.CityBuilder;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Ashley.Systems.CitySystems.CityBGSystem;
import io.github.slash_and_rule.Bases.GameScreen;
import io.github.slash_and_rule.Dungeon_Crawler.DungeonCrawlerScene;
import io.github.slash_and_rule.Utils.AtlasManager;

public class CityBuilderScene extends GameScreen {
    public DungeonCrawlerScene dungeonCrawlerScene;

    public CityBuilderScene(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager);
    }

    @Override
    public void init(LoadingScreen loader) {
        loader.schedule(() -> {
            assetManager.load("city/Grass.png", Texture.class);
            assetManager.finishLoading();
            addToEngine(loader, new CityBGSystem(gameCamera, assetManager, 0));
        });

        loader.schedule(() -> {
            for (Entity entity : CityData.buildings) {
                engine.addEntity(entity);
            }
        });
    }

    @Override
    protected void step(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            switchScreen = dungeonCrawlerScene;
        }
    }
}
