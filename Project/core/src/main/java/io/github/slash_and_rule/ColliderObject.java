package io.github.slash_and_rule;

import io.github.slash_and_rule.Bases.BasePhysicsObject;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class ColliderObject extends BasePhysicsObject {

    public ColliderObject(PhysicsScreen screen, InputManager inputManager, World world,
            float density, float friction, float restitution, float x, float y, Shape shape, BodyType type) {
        super(screen, inputManager, world, density, friction, restitution, x, y, type, shape);

    }

    @Override
    protected Shape getHitboxShape() {
        return null;
    }

    @Override
    public void dispose() {
        // Dispose of collider resources if necessary
    }
}
