package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class CollisionSystem extends EntitySystem {
    private static class GameContactListener implements ContactListener {
        private ArrayDeque<Contact> contacts = new ArrayDeque<>();
        private ArrayDeque<Contact> leaved = new ArrayDeque<>();

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
            contacts.add(contact);
        }

        @Override
        public void endContact(Contact contact) {
            leaved.add(contact);
        }

        public ArrayDeque<Contact> getContacts() {
            return contacts;
        }

        public ArrayDeque<Contact> getLeaved() {
            return leaved;
        }

        public void clearContacts() {
            contacts.clear();
        }
    };

    private final GameContactListener contactListener = new GameContactListener();

    private ComponentMapper<SensorComponent> sensorMapper = Mappers.sensorMapper;

    public CollisionSystem(int priority, World world) {
        super(priority);
        world.setContactListener(contactListener);
    }

    @Override
    public void update(float deltaTime) {
        for (Contact contact : contactListener.getContacts()) {
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();

            Object userDataA = contact.getFixtureA().getBody().getUserData();
            Object userDataB = contact.getFixtureB().getBody().getUserData();
            if (!(userDataA instanceof Entity) || !(userDataB instanceof Entity)) {
                System.out.println("Skipping - not entities");
                continue; // Skip if user data is not an Entity
            }
            Entity entityA = (Entity) userDataA;
            Entity entityB = (Entity) userDataB;

            handleWeapons(entityA, fixtureA, entityB, fixtureB);

            handleSensors(entityA, fixtureA, entityB, fixtureB);
        }
        contactListener.clearContacts();
        for (Contact contact : contactListener.getLeaved()) {
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();

            Object userDataA = fixtureA.getBody().getUserData();
            Object userDataB = fixtureB.getBody().getUserData();
            if (!(userDataA instanceof Entity) || !(userDataB instanceof Entity)) {
                continue; // Skip if user data is not an Entity
            }
            Entity entityA = (Entity) userDataA;
            Entity entityB = (Entity) userDataB;

            SensorComponent sensorA = sensorMapper.get(entityA);
            SensorComponent sensorB = sensorMapper.get(entityB);

            if (sensorA != null) {
                sensorA.isTriggered = false;
            } else if (sensorB != null) {
                sensorB.isTriggered = false;
            }
        }
    }

    private void handleWeapons(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        WeaponComponent weaponA = Mappers.weaponMapper.get(entityA);
        WeaponComponent weaponB = Mappers.weaponMapper.get(entityB);

        HealthComponent healthA = Mappers.healthMapper.get(entityA);
        HealthComponent healthB = Mappers.healthMapper.get(entityB);
        if (weaponA != null && healthB != null) {
            int damage = weaponA.damage;
            if (weaponA.chargeVal > 0f) {
                damage *= weaponA.chargetime / weaponA.chargeVal;
            }
            healthB.appliedDamage += damage;
        } else if (weaponB != null && healthA != null) {
            int damage = weaponB.damage;
            if (weaponB.chargetime > 0f) {
                damage *= weaponB.chargeVal / weaponB.chargetime;
            }
            healthA.appliedDamage += damage;
        }
    }

    private void handleSensors(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        SensorComponent sensorA = sensorMapper.get(entityA);
        SensorComponent sensorB = sensorMapper.get(entityB);

        if (sensorA != null) {
            sensorA.collisionHandler.handleCollision(entityA, fixtureA, entityB, fixtureB);
            sensorA.isTriggered = true;
        } else if (sensorB != null) {
            sensorB.collisionHandler.handleCollision(entityB, fixtureB, entityA, fixtureA);
            sensorB.isTriggered = true;
        }
    }
}
