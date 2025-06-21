package io.github.slash_and_rule.Interfaces;

import com.badlogic.ashley.core.Entity;

public interface CollisionHandler {
    public void handleCollision(Entity myEntity, Entity otherEntity);
}
