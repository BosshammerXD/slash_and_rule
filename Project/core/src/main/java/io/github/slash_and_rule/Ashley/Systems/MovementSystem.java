package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;

public class MovementSystem extends IteratingSystem {
    private ComponentMapper<MovementComponent> movementMapper = ComponentMapper.getFor(MovementComponent.class);
    private ComponentMapper<TransformComponent> transformMapper = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<PhysicsComponent> physicsMapper = ComponentMapper.getFor(PhysicsComponent.class);

    public MovementSystem(int priority) {
        super(Family.all(MovementComponent.class, TransformComponent.class).get(), priority);
    }

    @Override
    protected void processEntity(com.badlogic.ashley.core.Entity entity, float deltaTime) {
        MovementComponent movement = movementMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);
        PhysicsComponent collider = physicsMapper.get(entity);

        if (movement == null || transform == null) {
            return;
        }

        if (collider != null && collider.body != null) {
            transform.position.set(collider.body.getPosition());
        } else {
            transform.position.add(movement.velocity.scl(deltaTime));
        }
    }
}
