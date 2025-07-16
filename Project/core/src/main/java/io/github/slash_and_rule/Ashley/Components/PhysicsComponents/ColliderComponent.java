package io.github.slash_and_rule.Ashley.Components.PhysicsComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Fixture;

public class ColliderComponent implements Component {
    public Fixture[] colliders;

    public ColliderComponent() {
        colliders = new Fixture[0];
    }

    public ColliderComponent(Fixture... colliders) {
        this.colliders = colliders;
    }
}
