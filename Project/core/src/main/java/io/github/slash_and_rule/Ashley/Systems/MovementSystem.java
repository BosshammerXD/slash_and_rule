package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class MovementSystem extends IteratingSystem {

    public MovementSystem(int priority) {
        super(Family.all(MovementComponent.class, TransformComponent.class).exclude(InactiveComponent.class).get(),
                priority);
    }

    @Override
    protected void processEntity(com.badlogic.ashley.core.Entity entity, float deltaTime) {
        MovementComponent movement = Mappers.movementMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        PhysicsComponent collider = Mappers.physicsMapper.get(entity);

        if (movement == null || transform == null) {
            return;
        }
        transform.lastPosition.set(transform.position);
        if (collider != null && collider.body != null) {
            transform.position.set(collider.body.getPosition());
        } else {
            transform.position.add(movement.velocity.scl(deltaTime));
        }
    }
}
