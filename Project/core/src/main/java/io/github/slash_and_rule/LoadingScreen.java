package io.github.slash_and_rule;

import java.util.ArrayDeque;
import java.util.Stack;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.BaseScreen;

public class LoadingScreen extends BaseScreen {
    public class LoadingSchedule {
        public AssetManager assetManager;
        public ArrayDeque<Runnable> todo;
        public Stack<Thread> threads;

        public LoadingSchedule(AssetManager assetManager) {
            this.assetManager = assetManager;
            todo = new ArrayDeque<>();
            threads = new Stack<>();
        }
    }

    public BaseScreen nextScreen;
    private BaseScreen defaultScreen;
    private Main game;

    private LoadingSchedule loadingSchedule;

    public LoadingScreen(Main game, BaseScreen defaultScreen) {
        super();
        this.game = game;
        this.nextScreen = null; // Initially, there is no next screen set.
        this.defaultScreen = defaultScreen;
        this.viewport = new ExtendViewport(16, 9);
    }

    @Override
    public void show() {
        // Prepare the loading screen for display.
        this.loadingSchedule = new LoadingSchedule(game.assetManager);
        if (nextScreen != null) {
            nextScreen.setAssetManager(game.assetManager);
            nextScreen.init(this.loadingSchedule);
        } else {
            defaultScreen.init(this.loadingSchedule);
            System.out.println("No next screen set, using default screen.");
            nextScreen = defaultScreen;
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        // Additional rendering logic for the loading screen can be added here.
        if (nextScreen == null) {
            System.out.println("No next screen set, cannot proceed with loading.");
            return;
        }
        if (!loadingSchedule.assetManager.update()) {
            // If the asset manager is still loading assets, we can display a loading
            // message or animation.
            return;
        }
        if (loadingSchedule.todo != null && !loadingSchedule.todo.isEmpty()) {
            loadingSchedule.todo.pop().run();
            return;
        }
        if (!loadingSchedule.threads.isEmpty()) {
            if (loadingSchedule.threads.peek().isAlive()) {
                // If there are still threads running, we wait for them to finish.
                return;
            } else {
                loadingSchedule.threads.pop();
            }
        }
        game.setScreen(nextScreen);
    }
}
