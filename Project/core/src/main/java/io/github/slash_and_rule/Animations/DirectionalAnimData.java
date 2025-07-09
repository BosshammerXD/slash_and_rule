package io.github.slash_and_rule.Animations;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;

public abstract class DirectionalAnimData extends AnimData {
    private int dir = 0;
    protected FrameData[] frameDatas;

    public DirectionalAnimData(String atlasPath, FrameData[] frameDatas) {
        super(atlasPath, frameDatas[0]);
        this.frameDatas = frameDatas;
    }

    protected abstract int getDir(Vector2 moveVec);

    protected abstract Vector2 getVec(float deltaTime, Entity entity);

    @Override
    public void update(float deltaTime, Entity entity) {
        dir = getDir(getVec(deltaTime, entity));

        this.frames = frameDatas[dir];

        super.update(deltaTime, entity);
    }
}
