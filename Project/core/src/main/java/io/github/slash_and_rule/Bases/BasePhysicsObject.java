package io.github.slash_and_rule.Bases;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public abstract class BasePhysicsObject extends BaseGameObject {
    protected BodyDef bodyDef = new BodyDef();
    protected Body body;
    protected Fixture fixture;

    public BasePhysicsObject(PhysicsScreen screen,
            float density,
            float friction, float restitution, float start_x, float start_y, short category, short mask,
            BodyType BodyType) {
        super(screen);

        this.setup(screen.world, density, friction, restitution, start_x, start_y, category, mask, BodyType,
                this.getHitboxShape());
    }

    public BasePhysicsObject(PhysicsScreen screen,
            float density,
            float friction, float restitution, float start_x, float start_y, short category, short mask,
            BodyType BodyType, Shape hitboxShape) {
        super(screen);

        this.setup(screen.world, density, friction, restitution, start_x, start_y, category, mask, BodyType,
                hitboxShape);
    }

    private void setup(World world,
            float density,
            float friction, float restitution, float start_x, float start_y, short category, short mask,
            BodyType BodyType, Shape hitboxShape) {
        this.bodyDef.type = BodyType;
        this.bodyDef.position.set(start_x, start_y); // Set initial position of the physics object

        this.body = world.createBody(bodyDef);
        this.body.setLinearDamping(7.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = hitboxShape;
        fixtureDef.density = density; // Set the density of the physics object
        fixtureDef.friction = friction; // Set the friction of the physics object
        fixtureDef.restitution = restitution; // Set the restitution (bounciness) of the physics object
        fixtureDef.filter.categoryBits = category;
        fixtureDef.filter.maskBits = mask;
        this.fixture = body.createFixture(fixtureDef);

        hitboxShape.dispose(); // Dispose of the shape after creating the fixture
    }

    // Additional methods for physics interactions can be added here

    protected abstract Shape getHitboxShape(); // Abstract method to get the shape for the hitbox
}
