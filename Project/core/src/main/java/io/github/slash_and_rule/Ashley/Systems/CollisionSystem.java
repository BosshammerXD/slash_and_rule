package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayDeque;
import java.util.HashMap;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent.CollisionData;
import io.github.slash_and_rule.Utils.Mappers;

public class CollisionSystem extends EntitySystem {
    private static class GameContactListener implements ContactListener {
        private HashMap<Entity, ArrayDeque<CollisionData>> contacts = new HashMap<>();
        private HashMap<Entity, ArrayDeque<CollisionData>> leaved = new HashMap<>();
        private ArrayDeque<CollisionData> emptyDeque = new ArrayDeque<>();

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
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            Object userDataA = fixtureA.getBody().getUserData();
            Object userDataB = fixtureB.getBody().getUserData();
            if (!(userDataA instanceof Entity) || !(userDataB instanceof Entity)) {
                return;
            }
            Entity entityA = (Entity) userDataA;
            Entity entityB = (Entity) userDataB;
            if (Mappers.sensorMapper.has(entityA)) {
                contacts.putIfAbsent(entityA, new ArrayDeque<>());
                contacts.get(entityA).add(new CollisionData(fixtureA, fixtureB, entityB));
            }
            if (Mappers.sensorMapper.has(entityB)) {
                contacts.putIfAbsent(entityB, new ArrayDeque<>());
                contacts.get(entityB).add(new CollisionData(fixtureB, fixtureA, entityA));
            }
        }

        @Override
        public void endContact(Contact contact) {
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            Object userDataA = fixtureA.getBody().getUserData();
            Object userDataB = fixtureB.getBody().getUserData();
            if (!(userDataA instanceof Entity) || !(userDataB instanceof Entity)) {
                return;
            }
            Entity entityA = (Entity) userDataA;
            Entity entityB = (Entity) userDataB;
            if (Mappers.sensorMapper.has(entityA)) {
                leaved.putIfAbsent(entityA, new ArrayDeque<>());
                leaved.get(entityA).add(new CollisionData(fixtureA, fixtureB, entityB));
            }
            if (Mappers.sensorMapper.has(entityB)) {
                leaved.putIfAbsent(entityB, new ArrayDeque<>());
                leaved.get(entityB).add(new CollisionData(fixtureB, fixtureA, entityA));
            }
        }

        public ArrayDeque<CollisionData> getContacts(Entity entity) {
            return contacts.getOrDefault(entity, emptyDeque);
        }

        public ArrayDeque<CollisionData> getLeaved(Entity entity) {
            return leaved.getOrDefault(entity, emptyDeque);
        }

        public void clear() {
            contacts.clear();
            leaved.clear();
        }
    };

    private final GameContactListener contactListener = new GameContactListener();
    private ImmutableArray<Entity> sensors;

    public CollisionSystem(World world) {
        super(Globals.Priorities.Systems.Physics.Collision);
        world.setContactListener(contactListener);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        sensors = engine.getEntitiesFor(Family.all(SensorComponent.class)
                .exclude(InactiveComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity sensor : sensors) {
            SensorComponent sensorComp = Mappers.sensorMapper.get(sensor);

            ArrayDeque<CollisionData> contactsStarted = contactListener.getContacts(sensor);
            ArrayDeque<CollisionData> contactsEnded = contactListener.getLeaved(sensor);

            sensorComp.contactsStarted.clear();
            sensorComp.contactsStarted.addAll(contactsStarted);
            sensorComp.contactsEnded.clear();
            sensorComp.contactsEnded.addAll(contactsEnded);
        }
        contactListener.clear();
    }
}
