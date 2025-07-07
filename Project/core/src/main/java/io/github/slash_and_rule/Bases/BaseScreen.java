package io.github.slash_and_rule.Bases;

import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Interfaces.AsyncLoadable;

import java.util.ArrayDeque;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class BaseScreen implements Screen {

    public AsyncExecutor asyncExecutor = new AsyncExecutor(1);

    public ArrayDeque<AsyncResult<AsyncLoadable>> processingQueue = new ArrayDeque<>();
    public ArrayDeque<AsyncLoadable> asyncLoadableObjects = new ArrayDeque<>();
    public ArrayDeque<Disposable> disposableObjects = new ArrayDeque<>();

    protected AssetManager assetManager;

    public boolean halt = false;

    public OrthographicCamera camera = new OrthographicCamera();
    protected Viewport viewport;

    protected AtlasManager atlasManager;

    public BaseScreen(AssetManager assetManager, AtlasManager atlasManager) {
        this.assetManager = assetManager;
        this.atlasManager = atlasManager;
    }

    @Override
    public void show() {
        // This method is called when the screen is shown.
        // Initialize your screen here, such as loading assets or setting up the camera.
        this.viewport.apply();
        camera.update();
    }
    // Prepare your screen here.

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        if (assetManager != null && assetManager.update()) {
            atlasManager.AssetsLoaded();
            while (!asyncLoadableObjects.isEmpty()) {
                AsyncResult<AsyncLoadable> v = asyncLoadableObjects.pop().schedule(asyncExecutor);
                if (v != null) {
                    processingQueue.add(v);
                }
            }
        }

        for (int i = 0; i < processingQueue.size(); i++) {
            AsyncResult<AsyncLoadable> result = processingQueue.poll();
            if (result != null) {
                if (result.isDone()) {
                    AsyncLoadable obj = result.get();
                    if (obj != null) {
                        obj.loadDone();
                    }
                } else {
                    processingQueue.add(result);
                }
            }
        }

        preHalt(delta);

        if (halt) {
            return; // Skip rendering if the screen is halted
        }

        this.viewport.apply();
        ScreenUtils.clear(0, 0, 0, 1, true);
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height
        // are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a
        // normal size before updating.
        if (width <= 0 || height <= 0)
            return;
        this.viewport.update(width, height);
        camera.update();
        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
        halt = true;
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
        halt = false;
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        for (Disposable obj : disposableObjects) {
            obj.dispose();
        }
    }

    public void loadAsset(String assetPath, Class<?> assetType) {
        if (assetManager != null) {
            assetManager.load(assetPath, assetType);
        } else {
            Gdx.app.error("BaseScreen", "AssetManager is not initialized.");
        }
    }

    protected void preHalt(float delta) {
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }
}
