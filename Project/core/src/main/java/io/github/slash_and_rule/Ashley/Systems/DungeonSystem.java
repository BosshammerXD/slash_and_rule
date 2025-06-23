package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.slash_and_rule.Interfaces.CollisionHandler;

public class DungeonSystem extends EntitySystem {
    private Entity room;
    private Entity[] neighbours = new Entity[4]; // 0: left, 1: down, 2: right, 3: up

    public DungeonSystem(int priority) {
        super(priority);
        // TODO
    }

    @Override
    public void update(float deltaTime) {
        // TODO
    }

    private class DoorHandler implements CollisionHandler {
        @Override
        public void handleCollision(Entity myEntity, Fixture myFixture, Entity otherEntity, Fixture otherFixture) {
            Object myFixtureUserData = myFixture.getUserData();
            if (!(myFixtureUserData instanceof Integer)) {
                System.out.println("DungeonSystem: myFixtureUserData is not an Integer");
                return;
            }
            int doorId = (Integer) myFixtureUserData;

            // TODO Auto-generated method stub

        }
    }
}
