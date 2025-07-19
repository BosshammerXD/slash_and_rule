package io.github.slash_and_rule.Bases;

import java.util.function.Consumer;

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
    protected ExtendViewport gameViewport = new ExtendViewport(Globals.GameWidth, Globals.GameHeight, gameCamera);
    protected OrthographicCamera textCamera = new OrthographicCamera();
    protected ExtendViewport textViewport = new ExtendViewport(Globals.GameWidth * 32, Globals.GameHeight * 32,
            textCamera);
    protected Consumer<GameScreen> loadfunc;
    protected GameScreen switchScreen;

    public GameScreen(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager);
    }

    @Override
    public void show() {
        this.gameViewport.apply();
        this.gameCamera.update();
        super.show();
    }

    @Override
    public void init(LoadingScreen loader) {
        addToEngine(loader, new BGRenderSystem(gameViewport, gameCamera, atlasManager));
        addToEngine(loader, new MFRenderSystem(gameCamera, atlasManager));
        super.init(loader);
    }

    @Override
    public void resize(int width, int height) {
        this.gameViewport.update(width, height);
        this.gameCamera.update();
        this.textViewport.update(width, height);
        this.textCamera.update();
        super.resize(width, height);
    }

    @Override
    public final void render(float delta) {
        super.render(delta);
        step(delta);
        if (switchScreen != null) {
            GameScreen temp = switchScreen;
            switchScreen = null;
            loadfunc.accept(temp);
        }
    }

    protected abstract void step(float delta);

    public void setLoadfunc(Consumer<GameScreen> loader) {
        this.loadfunc = loader;
    }
}
