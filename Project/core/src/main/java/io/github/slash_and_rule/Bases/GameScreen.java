package io.github.slash_and_rule.Bases;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Ashley.Systems.RenderSystems.BGRenderSystem;
import io.github.slash_and_rule.Ashley.Systems.RenderSystems.MFRenderSystem;
import io.github.slash_and_rule.Utils.AtlasManager;

public abstract class GameScreen extends EntityScreen {
    protected OrthographicCamera gameCamera = new OrthographicCamera();
    protected ExtendViewport gameViewport;

    public GameScreen(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager);
        this.gameViewport = new ExtendViewport(Globals.GameWidth, Globals.GameHeight, gameCamera);
    }

    @Override
    public void show() {
        this.gameViewport.apply();
        this.gameCamera.update();
        super.show();
    }

    @Override
    public void init(LoadingScreen loader) {
        addToEngine(loader, new BGRenderSystem(gameViewport, gameCamera, atlasManager, Globals.BGRenderSystemPriority));
        addToEngine(loader, new MFRenderSystem(gameCamera, atlasManager, Globals.MFRenderSystemPriority));
        super.init(loader);
    }

    @Override
    public void resize(int width, int height) {
        this.gameViewport.update(width, height);
        this.gameCamera.update();
        super.resize(width, height);
    }
}
