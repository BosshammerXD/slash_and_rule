package io.github.slash_and_rule.Animations;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public abstract class DirectionalAnimData extends AnimData {
    private int index = 0;
    protected FrameData[] frameDatas;

    public DirectionalAnimData(String atlasPath, FrameData[] frameDatas) {
        super(atlasPath, frameDatas[0]);
        this.frameDatas = frameDatas;
    }

    protected void calcIndex(Vector2 moveVec) {
        if (moveVec.isZero()) {
            return;
        }
        int dir = index;
        if (Math.abs(moveVec.x) < Math.abs(moveVec.y)) {
            if (moveVec.y > 0) {
                dir = 3;
            } else {
                dir = 1;
            }
        } else {
            if (moveVec.x > 0) {
                dir = 2;
            } else {
                dir = 0;
            }
        }
        if (dir != index) {
            index = dir;
            animIndex = 0;
            stateTime = 0f;
        }
    }

    protected abstract Vector2 getVec(float deltaTime, Entity entity);

    @Override
    public void update(float deltaTime, Entity entity) {
        calcIndex(getVec(deltaTime, entity));
        this.frames = frameDatas[index];

        super.update(deltaTime, entity);
    }
}
