package io.github.slash_and_rule.Ashley.Components.PhysicsComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

public abstract class PhysicsComponent implements Component {
    public Body body;
    public Fixture fixture;
}
