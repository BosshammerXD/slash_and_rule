package io.github.slash_and_rule.Animations;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class mobEnAnimData extends DirectionalAnimData {
    private enum States {
        IDLE, WALKING, ATTACKING
    }
    private States state = States.IDLE;
    private FrameData[][] stateFrameDatas;

    public mobEnAnimData(String atlasPath, FrameData[][] stateFrameDatas) {
        // i would really like to check for the length of frames here, but it is not possible
        // because Java doesn't allow code before super() call
        super(atlasPath, stateFrameDatas[0]);
        this.stateFrameDatas = stateFrameDatas;
    }

    @Override
    protected int getDir(Vector2 moveVec) {
        int n = (moveVec.y < 0) ? 1 : 0;
        n |= (moveVec.x > 0) ? 2 : 0;
        n |= (moveVec.y > 0) ? 4 : 0;
        n |= (moveVec.x < 0) ? 8 : 0;
        return n;
    }

    @Override
    protected Vector2 getVec(float deltaTime, Entity entity) {
        Vector2 retVec = new Vector2(0,0);
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

    @Override
    public void update(float deltaTime, Entity entity) {
        super.update(deltaTime, entity);
    }
}
