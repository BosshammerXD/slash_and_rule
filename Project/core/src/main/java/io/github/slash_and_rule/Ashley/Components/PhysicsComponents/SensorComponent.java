package io.github.slash_and_rule.Ashley.Components.PhysicsComponents;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Fixture;

public class SensorComponent implements Component {
    public static class CollisionData {
        public Fixture myFixture;
        public Fixture otherFixture;
        public Entity entity;

        public CollisionData(Fixture myFixture, Fixture otherFixture, Entity entity) {
            this.myFixture = myFixture;
            this.otherFixture = otherFixture;
            this.entity = entity;
        }
    }

    public ArrayDeque<CollisionData> contactsStarted = new ArrayDeque<>();
    public ArrayDeque<CollisionData> contactsEnded = new ArrayDeque<>();

    public Fixture[] sensors;

    public SensorComponent() {
        sensors = new Fixture[0];
    }

    public SensorComponent(Fixture... sensors) {
        this.sensors = sensors;
    }
}
