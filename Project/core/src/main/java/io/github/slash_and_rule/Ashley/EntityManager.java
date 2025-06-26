package io.github.slash_and_rule.Ashley;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;

public class EntityManager {
    private static Engine engine;

    private static Entity entity;

    public static void setEngine(Engine engine) {
        EntityManager.engine = engine;
    }

    public static Entity getEntity() {
        checkEntity();
        return entity;
    }

    public static void start() {
        checkEngine();
        entity = engine.createEntity();
    }

    public static void add(Component component) {
        checkEngine();
        checkEntity();
        entity.add(component);
    }

    public static void add(Component... components) {
        checkEngine();
        checkEntity();
        for (Component component : components) {
            entity.add(component);
        }
    }

    public static Entity end() {
        checkEngine();
        checkEntity();
        engine.addEntity(entity);
        return entity;
    }

    private static void checkEngine() {
        if (engine == null) {
            throw new IllegalStateException(
                    "Engine is not set. Please call EntityManager.setEngine(engine) before using EntityManager.");
        }
    }

    private static void checkEntity() {
        if (entity == null) {
            throw new IllegalStateException(
                    "Entity is not created. Please call EntityManager.start() before using EntityManager.getEntity().");
        }
    }

    public static Entity makeEntity(Component... components) {
        checkEngine();
        Entity newEntity = engine.createEntity();
        for (Component component : components) {
            newEntity.add(component);
        }
        engine.addEntity(newEntity);
        return newEntity;
    }

    private Entity obj_entity;

    public EntityManager() {
        this.obj_entity = new Entity();
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
            throw new IllegalStateException("Engine is not set. Please call EntityManager.setEngine(engine) before using EntityManager.finish().");
        }
        if (obj_entity == null) {
            throw new IllegalStateException("Entity is not created. Please call EntityManager.reset() before using EntityManager.finish().");
        }
        engine.addEntity(obj_entity);
        obj_entity = null;
    }
}
