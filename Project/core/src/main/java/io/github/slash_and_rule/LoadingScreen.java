package io.github.slash_and_rule;

import java.util.ArrayDeque;

import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Bases.BaseScreen;

public class LoadingScreen extends BaseScreen {
    public BaseScreen nextScreen;
    private BaseScreen defaultScreen;
    private Main game;
    private ArrayDeque<Runnable> todo;

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
        todo = new ArrayDeque<>();
        if (nextScreen != null) {
            nextScreen.setAssetManager(game.assetManager);
            nextScreen.init(this.todo);
        } else {
            defaultScreen.init(this.todo);
            System.out.println("No next screen set, using default screen.");
            nextScreen = defaultScreen;
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        // Additional rendering logic for the loading screen can be added here.
        if (nextScreen != null && game.assetManager.update()) {
            if (todo != null && !todo.isEmpty()) {
                todo.pop().run();
                return;
            }
            game.setScreen(nextScreen);
        }
    }
}
