package io.github.slash_and_rule.Bases;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.InputManager;

public abstract class BasePhysicsObject extends BaseGameObject {
    protected BodyDef bodyDef = new BodyDef();
    protected Body body;
    protected Fixture fixture;

    public BasePhysicsObject(PhysicsScreen screen, InputManager inputManager, World world,
            float density,
            float friction, float restitution, int start_x, int start_y, BodyType BodyType) {
        super(screen, inputManager);

        this.bodyDef.type = BodyType;
        this.bodyDef.position.set(start_x, start_y); // Set initial position of the physics object

        this.body = world.createBody(bodyDef);
        this.body.setLinearDamping(7.5f);

        Shape hitboxShape = this.getHitboxShape(); // Get the shape for the hitbox, this should be overridden in
                                                   // subclasses

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = hitboxShape;
        fixtureDef.density = density; // Set the density of the physics object
        fixtureDef.friction = friction; // Set the friction of the physics object
        fixtureDef.restitution = restitution; // Set the restitution (bounciness) of the physics object
        this.fixture = body.createFixture(fixtureDef);

        hitboxShape.dispose(); // Dispose of the shape after creating the fixture
    }

    // Additional methods for physics interactions can be added here

    protected abstract Shape getHitboxShape(); // Abstract method to get the shape for the hitbox
}
