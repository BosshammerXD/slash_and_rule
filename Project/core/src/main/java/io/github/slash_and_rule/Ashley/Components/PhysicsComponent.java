package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class PhysicsComponent implements Component {
    public Body body;
    public Fixture fixture;
    public BodyType isStatic;
    public boolean isSensor = false;
    public boolean isActive = true;
    public boolean isBullet = false;
}
