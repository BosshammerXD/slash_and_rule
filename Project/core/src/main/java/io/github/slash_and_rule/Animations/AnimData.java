package io.github.slash_and_rule.Animations;

import com.badlogic.ashley.core.Entity;

public class AnimData {
    private String atlasPath;
    public int animIndex = 0;
    public float stateTime = 0f;
    protected FrameData frames;
    private float frameDuration = 0f;

    public AnimData(String atlasPath, FrameData frames) {
        this.atlasPath = atlasPath;
        this.frames = frames;
        frameDuration = frames.get(animIndex);
    }

    public void update(float deltaTime, Entity entity) {
        stateTime += deltaTime;
        while (stateTime >= frameDuration && animIndex >= 0) {
            animIndex++;
            stateTime -= frameDuration;
            if (animIndex >= frames.length()) {
                overflow();
            } else {
                frameDuration = frames.get(animIndex);
            }
        }
    }

    public String getAtlasPath() {
        return atlasPath;
    }

    public String getName() {
        if (frames == null) {
            return "";
        }
        return frames.getName();
    }

    public void overflow() {
        animIndex = 0;
        stateTime = 0f;
        frameDuration = frames.get(animIndex);
    }
}
