package io.github.slash_and_rule;

import java.util.ArrayDeque;
import java.util.function.Consumer;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Bases.GameScreen;
import io.github.slash_and_rule.Utils.AtlasManager;

public class LoadingScreen extends BaseScreen {
    private static class MsgRunnable implements Runnable {
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

    public ArrayDeque<ThreadData> threads;
    private ArrayDeque<MsgRunnable> schedule = new ArrayDeque<>();

    public GameScreen nextScreen;
    private GameScreen defaultScreen;
    private String msg = "";

    private Consumer<Screen> changeScreen;
    private boolean done = true;

    public LoadingScreen(GameScreen defaultScreen, AssetManager assetManager, AtlasManager atlasManager,
            Consumer<Screen> changeScreen) {
        super(assetManager, atlasManager);
        this.nextScreen = null; // Initially, there is no next screen set.
        this.defaultScreen = defaultScreen;
        this.changeScreen = changeScreen; // Set the consumer for changing screens.
        this.viewport = new ExtendViewport(16, 9);
    }

    @Override
    public void show() {
        super.show();
        this.threads = new ArrayDeque<>();
        if (nextScreen != null) {
            nextScreen.init(this);
        } else {
            defaultScreen.init(this);
            System.out.println("No next screen set, using default screen.");
            nextScreen = defaultScreen;
        }
    }

    @Override
    public void render(float delta) {
        if (nextScreen == null) {
            System.out.println("No next screen set, cannot proceed with loading.");
            return;
        }
        if (!assetManager.update()) {
            // If the asset manager is still loading assets, we can display a loading
            // message or animation.
            msg = "Loading assets: " + (int) (assetManager.getProgress() * 100) + "%";
            done = false;
        }
        super.render(delta);
        if (done) {
            System.out.println("Loading complete, switching to next screen.");
            changeScreen.accept(nextScreen);
        }
        done = true;
    }

    @Override
    protected void preHalt(float delta) {

        msg = "waiting for threads to finish...";
        if (!threads.isEmpty()) {
            done = false;
            if (threads.peek().thread.isAlive()) {
                // If there are still threads running, we wait for them to finish.
                return;
            } else {
                threads.pop().run();
                return;
            }
        }

        if (schedule != null && !schedule.isEmpty()) {
            done = false;
            schedule.pop().run();
            if (!schedule.isEmpty())
                msg = schedule.peek().msg;
        }

        if (!this.processingQueue.isEmpty()) {
            done = false;
            System.out.println("Processing queue is not empty, waiting for tasks to complete.");
        }
    }

    public void schedule(String msg, Runnable runnable) {
        if (runnable != null) {
            schedule.add(new MsgRunnable(msg, runnable));
        }
    }

    public void schedule(Runnable runnable) {
        schedule("", runnable);
    }

    public void load(GameScreen screen) {
        this.nextScreen = screen;
        changeScreen.accept(this);
    }
}
