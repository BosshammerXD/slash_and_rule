package io.github.slash_and_rule.Bases;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Systems.AnimationSystem;
import io.github.slash_and_rule.Ashley.Systems.InputSystem;
import io.github.slash_and_rule.Ashley.Systems.MovementSystem;
import io.github.slash_and_rule.Ashley.Systems.RenderSystem;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.Utils.AtlasManager;

public abstract class GameScreen extends BaseScreen {
    public ArrayDeque<Initalizable> loadableObjects = new ArrayDeque<>();

    protected Engine engine = new Engine();

    private InputSystem inputSystem = new InputSystem(Globals.InputSystemPriority);

    protected EntityManager entityManager = new EntityManager(engine);

    public GameScreen(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager);

        engine.addSystem(new AnimationSystem(Globals.AnimationSystemPriority, atlasManager));
        engine.addSystem(new RenderSystem(Globals.RenderSystemPriority, camera, atlasManager));
        engine.addSystem(inputSystem);
        engine.addSystem(new MovementSystem(Globals.MovementSystemPriority));
    }

    @Override
    public void show() {
        for (Initalizable data : loadableObjects) {
            data.show(assetManager);
        }
        super.show();
        inputSystem.setInputProcessor();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        engine.update(delta);
    }

    @Override
    public void hide() {
        assetManager.clear();
        Gdx.input.setInputProcessor(null);
    }

    public void init(LoadingScreen loader) {
        for (Initalizable data : loadableObjects) {
            data.init(loader);
        }
    }
}
