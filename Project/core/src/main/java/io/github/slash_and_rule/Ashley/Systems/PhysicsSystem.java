package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class PhysicsSystem extends EntitySystem {
    private World world;

    private ImmutableArray<Entity> moveables;

    public PhysicsSystem(int priority, World world) {
        super(priority);

        this.world = world;
    }

    @Override
    public void addedToEngine(Engine engine) {
        moveables = engine.getEntitiesFor(
                Family.all(PhysicsComponent.class, MovementComponent.class).get());

        engine.addEntityListener(
                Family.all(WeaponComponent.class, PhysicsComponent.class).get(),
                new EntityListener() {
                    @Override
                    public void entityAdded(Entity entity) {
                        WeaponComponent weapon = Mappers.weaponMapper.get(entity);
                        PhysicsComponent physics = Mappers.physicsMapper.get(entity);
                        weapon.body.setUserData(entity);
                        weapon.body.setTransform(physics.body.getPosition(), 0f);

                        RevoluteJointDef jointDef = new RevoluteJointDef();
                        jointDef.initialize(weapon.body, physics.body, physics.body.getPosition());
                        jointDef.collideConnected = false;
                        jointDef.enableMotor = false;
                        weapon.joint = (RevoluteJoint) world.createJoint(jointDef);
                    }

                    @Override
                    public void entityRemoved(Entity entity) {
                        WeaponComponent weapon = Mappers.weaponMapper.get(entity);
                        if (weapon.joint != null) {
                            world.destroyJoint(weapon.joint);
                            weapon.joint = null;
                        }
                        if (weapon.body != null) {
                            world.destroyBody(weapon.body);
                            weapon.body.setUserData(null);
                            weapon.body = null;
                        }
                    }
                });

        engine.addEntityListener(
                Family.all(PhysicsComponent.class).get(),
                new EntityListener() {
                    @Override
                    public void entityAdded(Entity entity) {
                        PhysicsComponent component = Mappers.physicsMapper.get(entity);
                        component.body.setUserData(entity);
                    }

                    @Override
                    public void entityRemoved(Entity entity) {
                        remove(Mappers.physicsMapper.get(entity));
                    }

                    private void remove(PhysicsComponent component) {
                        if (component == null || component.body == null)
                            return;
                        world.destroyBody(component.body);
                        component.body = null;
                        component.fixtures = null;
                    }
                });
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : moveables) {
            PhysicsComponent collider = Mappers.physicsMapper.get(entity);
            MovementComponent movement = Mappers.movementMapper.get(entity);
            if (collider == null || collider.body == null || movement == null) {
                continue;
            }
            Vector2 velocity = new Vector2(movement.velocity);
            if (movement.knockback.len() > 0) {
                velocity.add(movement.knockback);
                movement.knockback.scl(0.9f);
                if (movement.knockback.len() < 0.01f) {
                    movement.knockback.setZero();
                }
            }

            collider.body.setLinearVelocity(velocity);
        }

        world.step(deltaTime, 6, 2);
    }
}
