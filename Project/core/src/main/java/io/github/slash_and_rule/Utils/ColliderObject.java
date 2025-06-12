package io.github.slash_and_rule.Utils;

import io.github.slash_and_rule.Bases.BasePhysicsObject;
import io.github.slash_and_rule.Bases.PhysicsScreen;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class ColliderObject extends BasePhysicsObject {

    public ColliderObject(PhysicsScreen screen,
            float density, float friction, float restitution, float x, float y, short category, short mask, Shape shape,
            BodyType type) {
        super(screen, density, friction, restitution, x, y, category, mask, type, shape);

    }

    @Override
    protected Shape getHitboxShape() {
        return null;
    }

    public Body getBody() {
        return body;
    }

}
