package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;

public class CollisionSystem extends EntitySystem {
    private static class GameContactListener implements ContactListener {
        private ArrayDeque<Contact> contacts = new ArrayDeque<>();

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
            // TODO Auto-generated method stub
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
            // TODO Auto-generated method stub
        }

        @Override
        public void beginContact(Contact contact) {
            // TODO Auto-generated method stub
            contacts.add(contact);
        }

        @Override
        public void endContact(Contact contact) {
            // TODO Auto-generated method stub
        }

        public ArrayDeque<Contact> getContacts() {
            return contacts;
        }

        public void clearContacts() {
            contacts.clear();
        }
    };

    private final GameContactListener contactListener = new GameContactListener();

    private ComponentMapper<SensorComponent> sensorMapper = ComponentMapper.getFor(SensorComponent.class);

    public CollisionSystem(int priority, World world) {
        super(priority);
        world.setContactListener(contactListener);
    }

    @Override
    public void update(float deltaTime) {
        for (Contact contact : contactListener.getContacts()) {
            Object userDataA = contact.getFixtureA().getBody().getUserData();
            Object userDataB = contact.getFixtureB().getBody().getUserData();
            if (!(userDataA instanceof Entity) || !(userDataB instanceof Entity)) {
                continue; // Skip if user data is not an Entity
            }
            Entity entityA = (Entity) userDataA;
            Entity entityB = (Entity) userDataA;

            SensorComponent sensorA = sensorMapper.get(entityA);
            SensorComponent sensorB = sensorMapper.get(entityB);

            if (sensorA != null && sensorA.collisionHandler != null) {
                sensorA.collisionHandler.handleCollision(entityA, entityB);
            } else if (sensorB != null && sensorB.collisionHandler != null) {
                sensorB.collisionHandler.handleCollision(entityB, entityA);
            }
        }
        contactListener.clearContacts();
    }
}
