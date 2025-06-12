package io.github.slash_and_rule;

import java.util.ArrayDeque;
import java.util.Stack;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.BaseScreen;

public class LoadingScreen extends BaseScreen {
    public static class MsgRunnable implements Runnable {
        public String msg;
        private Runnable runnable;

        public MsgRunnable(String msg, Runnable runnable) {
            this.msg = msg;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }
    }

    public static class ThreadData implements Runnable {
        private Runnable runnable;
        public Thread thread;

        public ThreadData(Runnable runnable, Runnable after) {
            this.runnable = after;
            this.thread = new Thread(runnable);
            this.thread.start();
        }

        public ThreadData(Thread thread, Runnable after) {
            this.thread = thread;
            this.runnable = after;
        }

        public ThreadData(Runnable runnable) {
            this.runnable = null;
            this.thread = new Thread(runnable);
            this.thread.start();
        }

        public ThreadData(Thread thread) {
            this.thread = thread;
            this.runnable = null;
        }

        @Override
        public void run() {
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public static class LoadingSchedule {
        public AssetManager assetManager;
        public ArrayDeque<MsgRunnable> todo;
        public Stack<ThreadData> threads;

        public LoadingSchedule(AssetManager assetManager) {
            this.assetManager = assetManager;
            todo = new ArrayDeque<>();
            threads = new Stack<>();
        }
    }

    public BaseScreen nextScreen;
    private BaseScreen defaultScreen;
    private Main game;
    private String msg = "";

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
            msg = "Loading assets: " + (int) (loadingSchedule.assetManager.getProgress() * 100) + "%";
            return;
        }
        if (loadingSchedule.todo != null && !loadingSchedule.todo.isEmpty()) {
            loadingSchedule.todo.pop().run();
            if (!loadingSchedule.todo.isEmpty()) {
                msg = loadingSchedule.todo.peek().msg;
            }
            return;
        }
        msg = "waiting for threads to finish...";
        if (!loadingSchedule.threads.isEmpty()) {
            if (loadingSchedule.threads.peek().thread.isAlive()) {
                // If there are still threads running, we wait for them to finish.
                return;
            } else {
                loadingSchedule.threads.pop().run();
            }
        }

        if (!this.processingQueue.isEmpty()) {
            return;
        }

        game.setScreen(nextScreen);
    }
}
