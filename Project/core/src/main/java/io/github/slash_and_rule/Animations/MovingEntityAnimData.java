package io.github.slash_and_rule.Animations;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class MovingEntityAnimData extends DirectionalAnimData {
    private Vector2 lastPos = new Vector2(0f, 0f);

    private enum States {
        IDLE(0), WALKING(1), ATTACKING(2);

        public final int value;

        States(int value) {
            this.value = value;
        }
    }

    private States state = States.IDLE;
    private States lastState = States.IDLE;
    private FrameData[][] stateFrameDatas;
    private boolean looped = true;

    public MovingEntityAnimData(String atlasPath, FrameData[][] stateFrameDatas, TextureData textureData) {
        super(stateFrameDatas[0], textureData);
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
        if (state == States.ATTACKING && !looped) {
            return;
        }
        WeaponComponent weapon = Mappers.weaponMapper.get(entity);

        if (weapon != null && weapon.time == 0f && weapon.state == WeaponComponent.WeaponStates.COOLDOWN) {
            looped = false;
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
        TransformComponent transform = Mappers.transformMapper.get(entity);
        Vector2 move = Vector2.Zero.cpy();
        if (transform != null) {
            move.set(transform.position).sub(lastPos);
            lastPos.set(transform.position);
        }
        if (state != States.ATTACKING && move.len() < 0.01f) {
            state = States.IDLE;
        }
        this.frameDatas = stateFrameDatas[state.value];
        if (lastState != state) {
            lastState = state;
            super.overflow();
        }
        super.update(deltaTime, entity);
    }

    @Override
    public void overflow() {
        looped = true;
        super.overflow();
    }
}
