package io.github.slash_and_rule.Ashley.Builder;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;

public final class CompBuilders {
    public static class CompBuilder<T extends Component> {
        private T component;
        public CompBuilder(T component) {
            this.component = component;
        }

        public T get() {
            return component;
        }

        public void add(Entity entity) {
            entity.add(component);
        }
    }

    public static CompBuilder<TransformComponent> buildTransform(Vector2 position, float rotation, float z) {
        TransformComponent transformComponent = new TransformComponent();
        transformComponent.position.set(position);
        transformComponent.rotation = rotation;
        transformComponent.z = z;
        return new CompBuilder<>(transformComponent);
    }

    public static CompBuilder<TransformComponent> buildTransform(Vector2 position, float rotation) {
        return buildTransform(position, rotation, 0f);
    }

    public static CompBuilder<MovementComponent> buildMovement(Vector2 velocity, float maxSpeed) {
        MovementComponent movementComponent = new MovementComponent();
        movementComponent.velocity.set(velocity);
        movementComponent.max_speed = maxSpeed;
        return new CompBuilder<>(movementComponent);
    }

    public static CompBuilder<MovementComponent> buildMovement(float maxSpeed) {
        return buildMovement(new Vector2(0, 0), maxSpeed);
    }

    public static CompBuilder<HealthComponent> buildHealth(int maxHealth, int currentHealth) {
        HealthComponent healthComponent = new HealthComponent();
        healthComponent.maxHealth = maxHealth;
        healthComponent.health = currentHealth;
        return new CompBuilder<>(healthComponent);
    }

    public static CompBuilder<HealthComponent> buildHealth(int maxHealth) {
        return buildHealth(maxHealth, maxHealth);
    }


}
