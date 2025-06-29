package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.WeaponStates;

public class WeaponSystem extends IteratingSystem {
    public WeaponSystem(int priority) {
        super(Family.all(WeaponComponent.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        WeaponComponent weapon = entity.getComponent(WeaponComponent.class);
        if (weapon.state == WeaponStates.CHARGING && weapon.time <= weapon.chargetime) {
            weapon.chargetime += deltaTime;
        }
        if (weapon.chargetime > weapon.chargeVal) {
            weapon.chargetime = weapon.chargeVal;
        }

        if (weapon.state == WeaponStates.ATTACKING) {
            weapon.time = 0f;
            weapon.state = WeaponStates.COOLDOWN;

            attack(weapon);
        } else if (weapon.state == WeaponStates.COOLDOWN) {
            weapon.time += deltaTime;

            stepAttack(weapon);

            if (weapon.time >= weapon.cooldown) {
                weapon.state = WeaponStates.IDLE;
                weapon.time = 0f;
            }
        }

        rotateWeapon(weapon);
    }

    private void attack(WeaponComponent weapon) {
        // TODO: Initiate Attack Logic needed?
    }

    private void stepAttack(WeaponComponent weapon) {
        // TODO: Perform Attack frames
    }

    private void rotateWeapon(WeaponComponent weapon) {
        if (weapon.joint == null) {
            return;
        }
        weapon.body.setTransform(weapon.body.getPosition(), weapon.target.angleRad());

    }
}
