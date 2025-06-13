package io.github.slash_and_rule.Bases;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.Utils.SensorObject;

public abstract class PhysicsScreen extends BaseScreen {
    protected World world = new World(new Vector2(0, 0), true);
    protected Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();

    public ArrayList<SensorObject> sensors = new ArrayList<>();

    protected ContactListener contactListener = new ContactListener() {
        private ArrayList<SensorObject> sensorObjects = sensors;

        @Override
        public void beginContact(Contact contact) {
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();

            checkSensor(fixtureA, fixtureB);
            checkSensor(fixtureB, fixtureA);
        }

        private void checkSensor(Fixture sensorCandidate, Fixture other) {
            Object data = sensorCandidate.getUserData();

            if (!(data instanceof String)) {
                return;
            }
            String sensorName = (String) data;
            for (SensorObject sensor : sensorObjects) {
                if (sensor.name.equals(sensorName)) {
                    // Handle the sensor contact
                    sensor.onContact(other);
                    return;
                }
            }
        }

        @Override
        public void endContact(Contact contact) {
        };

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
        };

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
        };
    };

    public PhysicsScreen(boolean debug) {
        super();
        // Initialize the Box2D world and debug renderer
        world.setContactListener(contactListener);
        debugRenderer.setDrawBodies(debug);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        // Update the Box2D world
        camera.update();
        debugRenderer.render(world, camera.combined);
        world.step(delta, 6, 2);
    }

    public World getWorld() {
        return world;
    }
}
