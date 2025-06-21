package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;

public class PhysicsSystem extends EntitySystem {
    private World world;

    private ComponentMapper<PhysicsComponent> physicsMapper = ComponentMapper.getFor(PhysicsComponent.class);

    private ComponentMapper<MovementComponent> movementMapper = ComponentMapper.getFor(MovementComponent.class);

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
                Family.all(PhysicsComponent.class).get(),
                new EntityListener() {
                    @Override
                    public void entityAdded(Entity entity) {
                    }

                    @Override
                    public void entityRemoved(Entity entity) {
                        remove(physicsMapper.get(entity));
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
            PhysicsComponent collider = physicsMapper.get(entity);
            MovementComponent movement = movementMapper.get(entity);
            if (collider == null || collider.body == null || movement == null) {
                continue;
            }
            collider.body.setLinearVelocity(movement.velocity);
        }

        world.step(deltaTime, 6, 2);
    }
}
