package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class HealthSystem extends IteratingSystem {
    public HealthSystem(int priority) {
        super(Family.all(HealthComponent.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // TODO: Implement something to handle what to do when entity dead

        HealthComponent health = Mappers.healthMapper.get(entity);
        if (health.time >= 0f) {
            health.time += deltaTime;
            if (health.time >= health.invulnerabilityTime) {
                health.time = -1f;
                health.appliedDamage = 0;
            }
        } else if (health.appliedDamage > 0) {
            health.health -= health.appliedDamage;
            health.appliedDamage = 0;
            health.time = 0f;
        }
    }
}
