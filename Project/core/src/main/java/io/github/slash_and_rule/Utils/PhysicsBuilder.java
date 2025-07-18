package io.github.slash_and_rule.Utils;

import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsBuilder {
    private World world;

    public void setWorld(World world) {
        this.world = world;
    }

    public PhysicsBuilder(World world) {
        this.world = world;
    }

    public Body makeBody(Vector2 position, BodyType bodyType, float friction,
            boolean isActive) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = bodyType;
        bodyDef.active = isActive;

        Body body = world.createBody(bodyDef);
        body.setLinearDamping(friction);

        return body;
    }

    public Body makeBody(BodyType bodyType, float friction, boolean isActive) {
        return makeBody(Vector2.Zero.cpy(), bodyType, friction, isActive);
    }

    public Fixture addFixture(Body body, Shape shape, float density, float friction, float restitution, short category,
            short mask, boolean isSensor) {
        Fixture fixture = body
                .createFixture(makeFixtureDef(shape, density, friction, restitution, category, mask, isSensor));
        shape.dispose(); // Dispose of the shape after creating the fixture
        return fixture;
    }

    public Fixture addFixture(Body body, Shape shape, float density, float restitution, short category, short mask,
            boolean isSensor) {
        return addFixture(body, shape, density, 0f, restitution, category, mask, isSensor);
    }

    public Fixture addFixture(Body body, Shape shape, float density, short category, short mask, boolean isSensor) {
        return addFixture(body, shape, density, 0f, 0f, category, mask, isSensor);
    }

    public Fixture addFixture(Body body, Shape shape, short category, short mask, boolean isSensor) {
        return addFixture(body, shape, 0f, 0f, 0f, category, mask, isSensor);
    }

    public Fixture addFixture(Body body, Consumer<FixtureDef> modifier) {
        FixtureDef fixtureDef = new FixtureDef();
        modifier.accept(fixtureDef);
        Fixture fixture = body.createFixture(fixtureDef);
        fixtureDef.shape.dispose();
        return fixture;
    }

    private FixtureDef makeFixtureDef(Shape shape, float density, float friction, float restitution, short category,
            short mask, boolean isSensor) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;
        fixtureDef.isSensor = isSensor;

        fixtureDef.filter.categoryBits = category;
        fixtureDef.filter.maskBits = mask;

        return fixtureDef;
    }
}
