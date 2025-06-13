package io.github.slash_and_rule.Bases;

import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.Interfaces.Updatetable;
import io.github.slash_and_rule.Utils.Generation;
import io.github.slash_and_rule.Interfaces.AsyncLoadable;
import io.github.slash_and_rule.Interfaces.Displayable;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.Interfaces.Pausable;

import java.util.ArrayDeque;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class BaseScreen implements Screen {
    public AsyncExecutor asyncExecutor = new AsyncExecutor(1);

    public ArrayDeque<AsyncResult<AsyncLoadable>> processingQueue = new ArrayDeque<>();

    protected AssetManager assetManager = null;

    public ArrayList<Initalizable> loadableObjects = new ArrayList<>();

    public ArrayList<Displayable> drawableObjects = new ArrayList<>();
    public ArrayList<Displayable> drawableSprites = new ArrayList<>();
    public ArrayList<Updatetable> updatableObjects = new ArrayList<>();
    public ArrayList<Pausable> pausableObjects = new ArrayList<>();

    public ArrayList<Disposable> disposableObjects = new ArrayList<>();

    public ArrayDeque<AsyncLoadable> asyncLoadableObjects = new ArrayDeque<>();

    public boolean halt = false;

    protected InputManager inputManager = new InputManager();
    protected SpriteBatch batch = new SpriteBatch();

    public OrthographicCamera camera = new OrthographicCamera();
    protected Viewport viewport;

    public ArrayDeque<Runnable> schedule = new ArrayDeque<>();

    public BaseScreen(AssetManager assetManager) {
        Gdx.input.setInputProcessor(inputManager);
        this.assetManager = assetManager;
    }

    @Override
    public void show() {
        // This method is called when the screen is shown.
        // Initialize your screen here, such as loading assets or setting up the camera.
        for (Initalizable data : loadableObjects) {
            data.show(assetManager);
        }
        this.viewport.apply();
        camera.update();
    }
    // Prepare your screen here.

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        if (assetManager != null && assetManager.update()) {
            while (!asyncLoadableObjects.isEmpty()) {
                AsyncResult<AsyncLoadable> v = asyncLoadableObjects.pop().schedule(asyncExecutor);
                if (v != null) {
                    processingQueue.add(v);
                }
            }
        }

        if (!schedule.isEmpty()) {
            Runnable task = schedule.pop();
            if (task != null) {
                task.run();
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

        if (halt) {
            return; // Skip rendering if the screen is halted
        }

        for (Updatetable obj : updatableObjects) {
            obj.update(delta);
        }
        this.viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        ScreenUtils.clear(0, 0, 0, 1, true);

        for (Displayable obj : drawableObjects) {
            obj.draw(batch);
        }

        batch.begin();
        for (Displayable obj : drawableSprites) {
            obj.draw(batch);
        }
        batch.end();
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
        for (Pausable obj : pausableObjects) {
            obj.pause();
        }
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
        for (Pausable obj : pausableObjects) {
            obj.resume();
        }
    }

    @Override
    public void hide() {
        assetManager.clear();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        for (Disposable obj : disposableObjects) {
            obj.dispose();
        }
    };

    public void loadAsset(String assetPath, Class<?> assetType) {
        if (assetManager != null) {
            assetManager.load(assetPath, assetType);
        } else {
            Gdx.app.error("BaseScreen", "AssetManager is not initialized.");
        }
    }

    public AssetManager getAssetManager() {
        if (assetManager == null) {
            assetManager = new AssetManager();
        }
        return assetManager;
    }

    public void schedule_generation(Runnable callback, Generation generation, int generationValue) {
        schedule.add(() -> {
            if (generation.get() != generationValue) {
                return; // Skip if the generation value does not match
            }
            callback.run();
        });
    }
}
