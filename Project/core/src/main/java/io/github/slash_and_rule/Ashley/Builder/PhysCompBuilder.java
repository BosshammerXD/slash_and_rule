package io.github.slash_and_rule.Ashley.Builder;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;

import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Bases.BaseCompBuilder;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

public class PhysCompBuilder extends BaseCompBuilder<PhysicsComponent> {
    public static final boolean NotSensor = false;
    public static final boolean IsSensor = true;
    private PhysicsBuilder physicsBuilder;

    public PhysCompBuilder(PhysicsBuilder physicsBuilder) {
        this.physicsBuilder = physicsBuilder;
    }

    public void begin(Vector2 position, BodyType bodyType, float friction, boolean isActive) {
        begin(new PhysicsComponent());
        comp.body = physicsBuilder.makeBody(position, bodyType, friction, isActive);
    }

    public void begin(Vector2 position, BodyType bodyType, float friction) {
        this.begin(position, bodyType, friction, true);
    }

    public void begin(BodyType bodyType, float friction, boolean isActive) {
        begin(new PhysicsComponent());
        comp.body = physicsBuilder.makeBody(bodyType, friction, isActive);
    }

    public Body getBody() {
        checkBuilding();
        return comp.body;
    }

    public Fixture add(String name, Shape shape, float density, float restitution, short category, short mask,
            boolean isSensor) {
        checkBuilding();
        Fixture fixture = physicsBuilder.addFixture(comp.body, shape, density, restitution, category, mask, isSensor);
        comp.fixtures.put(name, fixture);
        return fixture;
    }

    public Fixture add(String name, Shape shape, float density, short category, short mask, boolean isSensor) {
        return add(name, shape, density, 0f, category, mask, isSensor);
    }

    public Fixture add(String name, Shape shape, short category, short mask, boolean isSensor) {
        return add(name, shape, 0f, 0f, category, mask, isSensor);
    }

    public Fixture asyncAdd(PhysicsComponent comp, String name, Shape shape, float density, float restitution,
            short category, short mask,
            boolean isSensor) {
        Fixture fixture = physicsBuilder.addFixture(comp.body, shape, density, restitution, category, mask, isSensor);
        comp.fixtures.put(name, fixture);
        return fixture;
    }

    public Fixture asyncAdd(PhysicsComponent comp, String name, Shape shape, float density, short category, short mask,
            boolean isSensor) {
        return asyncAdd(comp, name, shape, density, 0f, category, mask, isSensor);
    }

    public Fixture asyncAdd(PhysicsComponent comp, String name, Shape shape, short category, short mask,
            boolean isSensor) {
        return asyncAdd(comp, name, shape, 0f, 0f, category, mask, isSensor);
    }

    public PhysicsComponent make(Vector2 position, BodyType bodyType, float friction, boolean isActive) {
        begin(position, bodyType, friction, isActive);
        return end();
    }

    public PhysicsComponent make(BodyType bodyType, float friction, boolean isActive) {
        begin(bodyType, friction, isActive);
        return end();
    }
}