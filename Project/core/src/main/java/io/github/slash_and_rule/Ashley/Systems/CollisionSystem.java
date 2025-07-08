package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayDeque;
import java.util.function.Function;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class CollisionSystem extends EntitySystem {
    private static class Tuple<A> {
        public A first;
        public A second;

        public Tuple(A first, A second) {
            this.first = first;
            this.second = second;
        }

        public <B> Tuple<B> map(Function<A, B> function) {
            return new Tuple<>(function.apply(first), function.apply(second));
        }

        public Tuple<A> flip() {
            A temp = first;
            first = second;
            second = temp;
            return this;
        }
    }

    private static class GameContactListener implements ContactListener {
        private ArrayDeque<Tuple<Fixture>> contacts = new ArrayDeque<>();
        private ArrayDeque<Tuple<Fixture>> leaved = new ArrayDeque<>();

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
            contacts.add(new Tuple<Fixture>(contact.getFixtureA(), contact.getFixtureB()));
        }

        @Override
        public void endContact(Contact contact) {
            leaved.add(new Tuple<Fixture>(contact.getFixtureA(), contact.getFixtureB()));
        }

        public ArrayDeque<Tuple<Fixture>> getContacts() {
            return contacts;
        }

        public ArrayDeque<Tuple<Fixture>> getLeaved() {
            return leaved;
        }

        public void clearContacts() {
            contacts.clear();
        }
    };

    private final GameContactListener contactListener = new GameContactListener();

    public CollisionSystem(int priority, World world) {
        super(priority);
        world.setContactListener(contactListener);
    }

    // region: handlers
    @Override
    public void update(float deltaTime) {
        for (Tuple<Fixture> fixtures : contactListener.getContacts()) {
            handleContactStarted(getEntities(fixtures), fixtures);
        }
        contactListener.clearContacts();
        for (Tuple<Fixture> fixtures : contactListener.getLeaved()) {
            handleContactEnd(getEntities(fixtures), fixtures);
        }
    }

    private void handleContactStarted(Tuple<Entity> contact, Tuple<Fixture> fixtures) {
        if (contact == null) {
            return;
        }

        handleWeapons(contact, fixtures);

        handleSensorsEnter(contact, fixtures);
    }

    private void handleContactEnd(Tuple<Entity> contact, Tuple<Fixture> fixtures) {
        if (contact == null) {
            return;
        }
        handleSensorLeave(contact);
    }

    // endregion
    //
    //
    //
    // region: Weapon handling
    private void handleWeapons(Tuple<Entity> entities, Tuple<Fixture> fixtures) {
        Tuple<WeaponComponent> weapons = entities.map(Mappers.weaponMapper::get);

        Tuple<HealthComponent> healths = entities.map(Mappers.healthMapper::get);

        Tuple<PhysicsComponent> physics = entities.map(Mappers.physicsMapper::get);

        if (fixtures.first.getFilterData().categoryBits == Globals.HitboxCategory) {
            MovementComponent move = Mappers.movementMapper.get(entities.second);
            handleHit(weapons.first, healths.second, physics, move);
        } else if (fixtures.second.getFilterData().categoryBits == Globals.HitboxCategory) {
            MovementComponent move = Mappers.movementMapper.get(entities.first);
            handleHit(weapons.second, healths.first, physics.flip(), move);
        }
    }

    private void handleHit(WeaponComponent weapon, HealthComponent health, Tuple<PhysicsComponent> physics,
            MovementComponent move) {
        if (weapon == null || health == null) {
            return;
        }

        int damage = weapon.damage;
        if (weapon.chargeVal > 0f) {
            damage *= weapon.chargetime / weapon.chargeVal;
        }
        health.appliedDamage += damage;

        applyKnockback(physics, move, weapon.weight);
    }

    private void applyKnockback(Tuple<PhysicsComponent> physics, MovementComponent move, float knockback) {
        if (physics.first == null || physics.second == null || move == null) {
            return;
        }
        Vector2 srcPos = physics.first.body.getPosition();
        Vector2 targetPos = physics.second.body.getPosition();
        Vector2 direction = targetPos.cpy().sub(srcPos).nor();
        Vector2 knockbackForce = direction.scl(knockback);
        move.knockback.add(knockbackForce);
    }

    // endregion
    //
    //
    //
    // region: Sensor handling
    private void handleSensorsEnter(Tuple<Entity> entities, Tuple<Fixture> fixtures) {
        Tuple<SensorComponent> sensors = entities.map(Mappers.sensorMapper::get);

        if (sensors.first != null) {
            sensorEnter(sensors.first, entities, fixtures);
        } else if (sensors.second != null) {
            sensorEnter(sensors.second, entities.flip(), fixtures.flip());
            entities.flip();
            fixtures.flip();
        }
    }

    private void sensorEnter(SensorComponent sensor, Tuple<Entity> entities, Tuple<Fixture> fixtures) {
        sensor.collisionHandler.handleCollision(entities.first, fixtures.first, entities.second, fixtures.second);
        sensor.isTriggered = true;
    }

    private void handleSensorLeave(Tuple<Entity> entities) {
        Tuple<SensorComponent> sensors = entities.map(Mappers.sensorMapper::get);

        if (sensors.first != null) {
            sensors.first.isTriggered = false;
        } else if (sensors.second != null) {
            sensors.second.isTriggered = false;
        }
    }

    // endregion
    //
    //
    //
    // region: Helper Methods
    private Tuple<Entity> getEntities(Tuple<Fixture> fixtures) {
        Object userDataA = fixtures.first.getBody().getUserData();
        Object userDataB = fixtures.second.getBody().getUserData();
        if (!(userDataA instanceof Entity) || !(userDataB instanceof Entity)) {
            return null;
        }
        Entity entityA = (Entity) userDataA;
        Entity entityB = (Entity) userDataB;
        return new Tuple<>(entityA, entityB);
    }
    // endregion
}
