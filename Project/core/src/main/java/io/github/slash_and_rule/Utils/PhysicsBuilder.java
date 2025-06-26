package io.github.slash_and_rule.Utils;

import java.util.function.Consumer;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsBuilder {
    private World world;

    public PhysicsBuilder(World world) {
        this.world = world;
    }

    public Body makeBody(Entity entity, float start_x, float start_y, BodyType BodyType, float friction,
            boolean isActive) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(start_x, start_y);
        bodyDef.type = BodyType;
        bodyDef.active = isActive;

        Body body = world.createBody(bodyDef);
        body.setUserData(entity);
        body.setLinearDamping(friction);

        return body;
    }

    public Body makeBody(Entity entity, BodyType BodyType, float friction, boolean isActive) {
        return makeBody(entity, 0f, 0f, BodyType, friction, isActive);
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
