package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.WeaponStates;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.timedActions;
import io.github.slash_and_rule.Utils.Mappers;

public class WeaponSystem extends IteratingSystem {
    public WeaponSystem(int priority) {
        super(Family.all(WeaponComponent.class).exclude(InactiveComponent.class).get(), priority);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        WeaponComponent weapon = Mappers.weaponMapper.get(entity);
        MovementComponent movement = Mappers.movementMapper.get(entity);
        if (weapon.state == WeaponStates.CHARGING && weapon.time <= weapon.chargetime) {
            weapon.chargetime += deltaTime;
        }
        if (weapon.chargetime > weapon.chargeVal) {
            weapon.chargetime = weapon.chargeVal;
        }

        if (weapon.state == WeaponStates.ATTACKING) {
            movement.max_speed *= 0.5f;
            weapon.time = 0f;
            weapon.state = WeaponStates.COOLDOWN;
            rotateWeapon(weapon);
            attack(weapon);
        } else if (weapon.state == WeaponStates.COOLDOWN) {
            weapon.time += deltaTime;

            handleHitboxes(weapon);

            if (weapon.time >= weapon.cooldown) {
                movement.max_speed *= 2f;
                weapon.state = WeaponStates.IDLE;
                weapon.time = 0f;
                weapon.index = 0; // Reset index when returning to IDLE
            }
        }
    }

    private void attack(WeaponComponent weapon) {
        if (weapon.animData != null) {
            weapon.animData.trigger();
        }
        weapon.index = 0; // Reset index für neue Hitboxes
    }

    private void handleHitboxes(WeaponComponent weapon) {
        if (weapon.index >= weapon.fixtures.length) {
            return;
        }
        timedActions actions = weapon.fixtures[weapon.index];
        if (actions.time <= weapon.time) {
            actions.run();
            weapon.index++;
        }
    }

    private void rotateWeapon(WeaponComponent weapon) {
        if (weapon.joint == null) {
            return;
        }
        weapon.body.setTransform(weapon.body.getPosition(), weapon.target.angleRad());
        if (weapon.texture == null) {
            return;
        }
        weapon.texture.angle = weapon.target.angleDeg() - 45f;
    }
}
