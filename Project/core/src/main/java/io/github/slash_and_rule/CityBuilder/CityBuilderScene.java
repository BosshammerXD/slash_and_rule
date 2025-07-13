package io.github.slash_and_rule.CityBuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;

import io.github.slash_and_rule.Bases.GameScreen;
import io.github.slash_and_rule.Dungeon_Crawler.DungeonCrawlerScene;
import io.github.slash_and_rule.Utils.AtlasManager;

public class CityBuilderScene extends GameScreen {
    public DungeonCrawlerScene dungeonCrawlerScene;

    public CityBuilderScene(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager);
    }

    @Override
    protected void step(float delta) {
        // TODO Auto-generated method stub
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            switchScreen = dungeonCrawlerScene;
        }
    }
}
