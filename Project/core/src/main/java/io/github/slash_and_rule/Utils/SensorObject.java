package io.github.slash_and_rule.Utils;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import java.util.function.Consumer;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.Bases.BasePhysicsObject;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.InputManager;

public class SensorObject extends BasePhysicsObject {
    public String name;
    private Consumer<Fixture> contactHandler;

    public SensorObject(PhysicsScreen screen, InputManager inputManager, World world,
            float density, float friction, float restitution, float x, float y,
            short category, short mask, Shape shape, String name, Consumer<Fixture> contactHandler) {

        super(screen, inputManager, world, density, friction, restitution, x, y, category, mask,
                BodyType.StaticBody, shape);

        this.name = name;
        this.contactHandler = contactHandler;

        fixture.setSensor(true); // Set the fixture as a sensor
        fixture.setUserData(name); // Set user data for identification

        screen.sensors.add(this); // Add this sensor to the screen's sensor list
    }

    @Override
    protected Shape getHitboxShape() {
        return null; // Sensors typically do not have a hitbox shape
    }

    @Override
    public void dispose() {
        // Dispose of sensor resources if necessary
    }

    public void onContact(Fixture other) {
        // Handle contact with other objects
        // This method can be overridden in subclasses to define specific behavior
        if (contactHandler != null) {
            contactHandler.accept(other);
        }
    }

    public void setContactHandler(Consumer<Fixture> contactHandler) {
        this.contactHandler = contactHandler;
    }

    public Body getBody() {
        return body;
    }
}
