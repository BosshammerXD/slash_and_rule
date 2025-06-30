package io.github.slash_and_rule.Animations;

public class triggeredAnimData extends LoopedAnimData {
    public triggeredAnimData(String atlasPath, String name, float frameDuration) {
        super(atlasPath, name, frameDuration);
    }

    public void trigger() {
        this.animIndex = 0;
        this.stateTime = 0f;
    }

    @Override
    public void update(float deltaTime) {
        if (animIndex < 0) {
            return;
        }
        super.update(deltaTime);
    }

    @Override
    public void overflow() {
        this.animIndex = -1;
    }
}
