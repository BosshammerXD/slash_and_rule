package io.github.slash_and_rule.Animations;

import com.badlogic.ashley.core.Entity;

public class AnimData {
    private String atlasPath;
    public int animIndex = 0;
    public float stateTime = 0f;
    protected FrameData frames;

    public AnimData(String atlasPath, FrameData frames) {
        this.atlasPath = atlasPath;
        this.frames = frames;
    }

    public void update(float deltaTime, Entity entity) {
        float frameDuration = frames.get(animIndex);

        stateTime += deltaTime;
        while (stateTime >= frameDuration) {
            animIndex++;
            stateTime -= frameDuration;
        }
        if (animIndex < 0) {
            animIndex = 0;
        }
    }

    public String getAtlasPath() {
        return atlasPath;
    }

    public String getName() {
        return frames.getName();
    }

    public void overflow() {
        animIndex = 0;
        stateTime = 0f;
    }
}

