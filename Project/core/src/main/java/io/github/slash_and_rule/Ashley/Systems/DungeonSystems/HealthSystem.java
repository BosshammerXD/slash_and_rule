package io.github.slash_and_rule.Ashley.Systems.DungeonSystems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.InvulnerableComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class HealthSystem extends IteratingSystem {
    public HealthSystem() {
        super(Family.all(HealthComponent.class, SensorComponent.class).exclude(InactiveComponent.class).get(),
                Globals.Priorities.Systems.Health);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // TODO: Implement something to handle what to do when entity dead

        HealthComponent health = Mappers.healthMapper.get(entity);
        InvulnerableComponent invuln = Mappers.invulnerableMapper.get(entity);
        if (invuln != null) {
            if (invuln.duration > 0) {
                invuln.duration -= deltaTime;
            } else {
                entity.remove(InvulnerableComponent.class);
            }
            return;
        }
        SensorComponent sensComp = Mappers.sensorMapper.get(entity);

        for (SensorComponent.CollisionData data : sensComp.contactsStarted) {
            if (!data.myFixture.isSensor()
                    || data.otherFixture.getFilterData().categoryBits != Globals.Categories.Hitbox) {
                continue;
            }
            Entity otherEntity = data.entity;
            // TODO: expand for Projectiles
            WeaponComponent otherWeapon = Mappers.weaponMapper.get(otherEntity);
            if (otherWeapon == null) {
                return;
            }
            entity.add(new InvulnerableComponent(health.invulnerabilityTime));
            health.health -= otherWeapon.damage;
        }
        // else if (health.appliedDamage > 0) {
        // health.health -= health.appliedDamage;
        // health.time = 0f;
        // }
    }
}
