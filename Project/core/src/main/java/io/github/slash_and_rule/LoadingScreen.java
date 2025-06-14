package io.github.slash_and_rule;

import java.util.Stack;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Interfaces.AsyncLoadable;
import io.github.slash_and_rule.Interfaces.Initalizable;

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

    public Stack<ThreadData> threads;

    public BaseScreen nextScreen;
    private BaseScreen defaultScreen;
    private String msg = "";

    private Consumer<Screen> changeScreen;

    public LoadingScreen(BaseScreen defaultScreen, AssetManager assetManager, Consumer<Screen> changeScreen) {
        super(assetManager);
        this.nextScreen = null; // Initially, there is no next screen set.
        this.defaultScreen = defaultScreen;
        this.changeScreen = changeScreen; // Set the consumer for changing screens.
        this.viewport = new ExtendViewport(16, 9);
    }

    @Override
    public void show() {
        this.threads = new Stack<>();
        if (nextScreen != null) {
            runInits(nextScreen);
            System.out.println("loading AsyncList: " + this.asyncLoadableObjects.toString());
        } else {
            runInits(defaultScreen);
            System.out.println("No next screen set, using default screen.");
            nextScreen = defaultScreen;
        }
    }

    private void runInits(BaseScreen screen) {
        for (Initalizable data : screen.loadableObjects) {
            data.init(this);
        }
    }

    @Override
    public void render(float delta) {
        // super.render(delta);
        // Additional rendering logic for the loading screen can be added here.
        if (nextScreen == null) {
            System.out.println("No next screen set, cannot proceed with loading.");
            return;
        }
        if (!assetManager.update()) {
            // If the asset manager is still loading assets, we can display a loading
            // message or animation.
            msg = "Loading assets: " + (int) (assetManager.getProgress() * 100) + "%";
            return;
        }

        while (!asyncLoadableObjects.isEmpty()) {
            AsyncResult<AsyncLoadable> v = asyncLoadableObjects.pop().schedule(asyncExecutor);
            if (v != null) {
                processingQueue.add(v);
            }
        }

        if (schedule != null && !schedule.isEmpty()) {
            schedule.pop().run();
            if (schedule.peek() instanceof MsgRunnable) {
                msg = ((MsgRunnable) schedule.peek()).msg;
            }
            return;
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

        msg = "waiting for threads to finish...";
        if (!threads.isEmpty()) {
            if (threads.peek().thread.isAlive()) {
                // If there are still threads running, we wait for them to finish.
                return;
            } else {
                threads.pop().run();
                return;
            }
        }

        if (!this.processingQueue.isEmpty()) {
            System.out.println("Processing queue is not empty, waiting for tasks to complete.");
            return;
        }
        System.out.println("Loading complete, switching to next screen.");
        changeScreen.accept(nextScreen);
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub
        Gdx.input.setInputProcessor(null);
    }
}
