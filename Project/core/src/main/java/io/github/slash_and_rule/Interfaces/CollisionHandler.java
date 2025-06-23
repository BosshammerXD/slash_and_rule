package io.github.slash_and_rule.Interfaces;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Fixture;

public interface CollisionHandler {
    public void handleCollision(Entity myEntity, Fixture myFixture, Entity otherEntity, Fixture otherFixture);
}
