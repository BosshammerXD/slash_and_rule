package io.github.slash_and_rule.Bases;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;

import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Components.ChildComponent;
import io.github.slash_and_rule.Ashley.Components.ParentComponent;
import io.github.slash_and_rule.Ashley.Systems.AnimationSystem;
import io.github.slash_and_rule.Ashley.Systems.InputSystem;
import io.github.slash_and_rule.Ashley.Systems.MovementSystem;
import io.github.slash_and_rule.Ashley.Systems.StateSystem;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.Utils.AtlasManager;

public abstract class EntityScreen extends BaseScreen {
    public ArrayDeque<Initalizable> loadableObjects = new ArrayDeque<>();

    protected Engine engine = new Engine();

    private InputSystem inputSystem;

    protected EntityManager entityManager = new EntityManager(engine);

    public EntityScreen(AssetManager assetManager, AtlasManager atlasManager) {
        super(assetManager, atlasManager);
        engine.addEntityListener(Family.all(ParentComponent.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                ParentComponent parentComponent = entity.getComponent(ParentComponent.class);
                for (Entity child : parentComponent.children) {
                    if (child == null) {
                        continue;
                    }
                    ChildComponent childComponent = new ChildComponent();
                    childComponent.parent = entity;
                    child.add(childComponent);
                    engine.addEntity(child);
                }
            }

            @Override
            public void entityRemoved(Entity entity) {
                for (Entity child : entity.getComponent(ParentComponent.class).children) {
                    if (child == null) {
                        continue;
                    }
                    engine.removeEntity(child);
                }
            }
        });
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
        engine.removeAllEntities();
        engine.removeAllSystems();
        assetManager.clear();
        atlasManager.clear();
        Gdx.input.setInputProcessor(null);
    }

    public void init(LoadingScreen loader) {
        this.inputSystem = new InputSystem();
        addToEngine(loader, new AnimationSystem(atlasManager));
        addToEngine(loader, this.inputSystem);
        addToEngine(loader, new MovementSystem());
        addToEngine(loader, new StateSystem());
        for (Initalizable data : loadableObjects) {
            data.init(loader);
        }
    }

    protected void addToEngine(LoadingScreen loader, EntitySystem system) {
        loader.schedule("building Systems", () -> engine.addSystem(system));
    }
}
