package io.github.slash_and_rule.Animations;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class mobEnAnimData extends DirectionalAnimData {
    private enum States {
        IDLE(0), WALKING(1), ATTACKING(2);

        public final int value;

        States(int value) {
            this.value = value;
        }
    }

    private States state = States.IDLE;
    private FrameData[][] stateFrameDatas;

    public mobEnAnimData(String atlasPath, FrameData[][] stateFrameDatas) {
        super(atlasPath, stateFrameDatas[0]);
        this.stateFrameDatas = stateFrameDatas;
    }

    @Override
    protected Vector2 getVec(float deltaTime, Entity entity) {
        Vector2 retVec = new Vector2(0, 0);
        switch (state) {
            case States.ATTACKING:
                WeaponComponent weapon = Mappers.weaponMapper.get(entity);
                if (weapon == null) {
                    break;
                }
                retVec.set(weapon.target);
                break;
            default:
                MovementComponent movement = Mappers.movementMapper.get(entity);
                if (movement == null) {
                    break;
                }
                retVec.set(movement.velocity);
        }
        return retVec;
    }

    private void calcState(Entity entity) {
        WeaponComponent weapon = Mappers.weaponMapper.get(entity);

        if (weapon != null && weapon.time != 0f) {
            state = States.ATTACKING;
            return;
        }
        MovementComponent movement = Mappers.movementMapper.get(entity);
        if (movement != null && movement.velocity.len() > 0.01f) {
            state = States.WALKING;
        } else {
            state = States.IDLE;
        }
    }

    @Override
    public void update(float deltaTime, Entity entity) {
        calcState(entity);
        this.frameDatas = stateFrameDatas[state.value];
        super.update(deltaTime, entity);
    }
}
