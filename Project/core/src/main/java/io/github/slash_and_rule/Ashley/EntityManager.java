package io.github.slash_and_rule.Ashley;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;

public class EntityManager {
    private Engine engine;

    private Entity obj_entity;

    public EntityManager(Engine engine) {
        this.obj_entity = new Entity();
        this.engine = engine;
    }

    public Entity reset() {
        this.obj_entity = new Entity();
        return this.obj_entity;
    }

    public Entity build(Component... components) {
        for (Component component : components) {
            obj_entity.add(component);
        }
        return obj_entity;
    }

    public void finish() {
        if (engine == null) {
            throw new IllegalStateException(
                    "Engine is not set. Please call EntityManager.setEngine(engine) before using EntityManager.finish().");
        }
        if (obj_entity == null) {
            throw new IllegalStateException(
                    "Entity is not created. Please call EntityManager.reset() before using EntityManager.finish().");
        }
        engine.addEntity(obj_entity);
        obj_entity = null;
    }

    public Entity makeEntity(Component... components) {
        Entity entity = new Entity();
        for (Component component : components) {
            entity.add(component);
        }
        return entity;
    }

    public Entity addEntity(Component... components) {
        Entity entity = new Entity();
        for (Component component : components) {
            entity.add(component);
        }
        engine.addEntity(entity);
        return entity;
    }
}
