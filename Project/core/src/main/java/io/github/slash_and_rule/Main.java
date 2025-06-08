package io.github.slash_and_rule;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Dungeon_Crawler.DungeonCrawlerScene;

/**
 * aaaaa
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    public AssetManager assetManager = new AssetManager();
    private LoadingScreen loadingScreen;

    BaseScreen[] screens = new BaseScreen[10];

    @Override
    public void create() {
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(assetManager.getFileHandleResolver()));

        screens[0] = new DungeonCrawlerScene(assetManager);

        loadingScreen = new LoadingScreen(this, screens[0]);

        loadingScreen.nextScreen = screens[0];

        setScreen(loadingScreen);
    }
}