package io.github.slash_and_rule;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Dungeon_Crawler.DungeonCrawlerScene;
import io.github.slash_and_rule.Utils.AtlasManager;

/**
 * aaaaa
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    private AssetManager assetManager = new AssetManager();
    private AtlasManager atlasManager = new AtlasManager(assetManager);
    private LoadingScreen loadingScreen;

    BaseScreen[] screens = new BaseScreen[10];

    @Override
    public void create() {
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(assetManager.getFileHandleResolver()));

        screens[0] = new DungeonCrawlerScene(assetManager, atlasManager);

        loadingScreen = new LoadingScreen(screens[0], assetManager, atlasManager, screen -> setScreen(screen));

        loadingScreen.nextScreen = screens[0];

        setScreen(loadingScreen);
    }
}
