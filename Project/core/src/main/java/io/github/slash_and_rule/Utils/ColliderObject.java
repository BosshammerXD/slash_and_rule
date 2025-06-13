package io.github.slash_and_rule.Utils;

import io.github.slash_and_rule.Bases.BaseGameObject;
import io.github.slash_and_rule.Bases.PhysicsScreen;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class ColliderObject extends BaseGameObject implements Disposable {
    protected Body body;
    protected Fixture fixture;
    private World world;

    public ColliderObject(PhysicsScreen screen,
            float density,
            float friction, float restitution, float start_x, float start_y, short category, short mask,
            Shape hitboxShape,
            BodyType BodyType) {
        super(screen);

        this.world = screen.getWorld();

        makeBody(makeBodyDef(start_x, start_y, BodyType, true), friction);
        makeFixture(density, 0, restitution, category, mask, hitboxShape);
    }

    public ColliderObject(PhysicsScreen screen,
            float density,
            float friction, float restitution, float start_x, float start_y, short category, short mask,
            Shape hitboxShape,
            BodyType BodyType, boolean isActive) {
        super(screen);

        this.world = screen.getWorld();

        makeBody(makeBodyDef(start_x, start_y, BodyType, isActive), friction);
        makeFixture(density, 0, restitution, category, mask, hitboxShape);
    }

    private BodyDef makeBodyDef(float start_x, float start_y, BodyType BodyType, boolean isActive) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType;
        bodyDef.position.set(start_x, start_y); // Set initial position of the physics object
        bodyDef.active = isActive; // Set the body to be active or inactive
        return bodyDef;
    }

    private void makeBody(BodyDef bodyDef, float friction) {
        this.body = world.createBody(bodyDef);
        this.body.setLinearDamping(friction);
    }

    private void makeFixture(float density, float friction, float restitution, short category, short mask,
            Shape hitboxShape) {
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

    public Body getBody() {
        return body;
    }

    public Fixture getFixture() {
        return fixture;
    }

    @Override
    public void dispose() {
        if (body != null && world != null) {
            world.destroyBody(body);
        }
        body = null;
        fixture = null;
    }
}
