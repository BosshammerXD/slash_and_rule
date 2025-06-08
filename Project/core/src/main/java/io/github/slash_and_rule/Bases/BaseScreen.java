package io.github.slash_and_rule.Bases;

import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.Interfaces.Updatetable;
import io.github.slash_and_rule.Interfaces.Displayable;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.Interfaces.Pausable;

import java.util.ArrayList;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class BaseScreen implements Screen {
    private AssetManager assetManager = null;

    public ArrayList<Initalizable> loadableObjects = new ArrayList<>();

    public ArrayList<Displayable> drawableObjects = new ArrayList<>();
    public ArrayList<Displayable> drawableSprites = new ArrayList<>();
    public ArrayList<Updatetable> updatableObjects = new ArrayList<>();
    public ArrayList<Pausable> pausableObjects = new ArrayList<>();

    protected InputManager inputManager = new InputManager();
    protected SpriteBatch batch = new SpriteBatch();

    public OrthographicCamera camera = new OrthographicCamera();
    protected Viewport viewport;

    public BaseScreen() {
        Gdx.input.setInputProcessor(inputManager);
    }

    public void init(Stack<Runnable> todo) {
        for (Initalizable data : loadableObjects) {
            data.init(assetManager, todo);
        }
    };

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
        for (Updatetable obj : updatableObjects) {
            obj.update(delta);
        }
        this.viewport.apply();
        camera.update();
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
        // This method is called when another screen replaces this one.
        for (Displayable obj : drawableSprites) {
            obj.hide();
        }
    }

    @Override
    public void dispose() {
        for (Initalizable obj : loadableObjects) {
            obj.dispose();
        }
    };

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

}
