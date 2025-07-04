package io.github.slash_and_rule.Ashley.Components.PhysicsComponents;

import com.badlogic.ashley.core.Component;

import io.github.slash_and_rule.Interfaces.CollisionHandler;

public class SensorComponent implements Component {
    public CollisionHandler collisionHandler;
    public boolean isTriggered = false;

    public SensorComponent() {
        // Default constructor
    }

    public SensorComponent(CollisionHandler collisionHandler) {
        this.collisionHandler = collisionHandler;
    }
}
