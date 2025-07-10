package io.github.slash_and_rule.Animations;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;

public class AnimData {
    protected int animIndex = 0;
    protected float stateTime = 0f;
    protected FrameData frames;
    private float frameDuration = 0f;
    protected TextureData textureData;

    public AnimData(FrameData frames, TextureData textureData) {
        this.frames = frames;
        this.textureData = textureData;
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

    public final String getAtlasPath() {
        if (textureData == null) {
            return null;
        }
        return textureData.atlasPath;
    }

    public final String getName() {
        if (frames == null) {
            return "";
        }
        return frames.getName();
    }

    public final int getIndex() {
        return animIndex;
    }

    public final void setTexture(TextureRegion texture) {
        if (textureData != null) {
            textureData.texture = texture;
        }
    }

    public void overflow() {
        animIndex = 0;
        stateTime = 0f;
        frameDuration = frames.get(animIndex);
    }
}
