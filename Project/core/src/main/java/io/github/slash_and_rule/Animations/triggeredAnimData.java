package io.github.slash_and_rule.Animations;

import com.badlogic.ashley.core.Entity;

import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;

public class triggeredAnimData extends AnimData {
    private int stillframe;
    private boolean triggered = false;

    public triggeredAnimData(FrameData frames, int stillframe, TextureData textureData) {
        super(frames, textureData);
        this.stillframe = stillframe;
        this.animIndex = stillframe;
    }

    public void trigger() {
        this.animIndex = 0;
        this.stateTime = 0f;
        this.triggered = true;
    }

    @Override
    public void update(float deltaTime, Entity entity) {
        if (!triggered) {
            return;
        }
        super.update(deltaTime, entity);
    }

    @Override
    public void overflow() {
        this.animIndex = stillframe;
        this.triggered = false;
    }
}
