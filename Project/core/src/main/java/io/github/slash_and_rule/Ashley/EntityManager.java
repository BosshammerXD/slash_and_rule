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

    public static void end() {
        checkEngine();
        checkEntity();
        engine.addEntity(entity);
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
}
