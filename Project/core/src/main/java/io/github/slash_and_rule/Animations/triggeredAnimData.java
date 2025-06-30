package io.github.slash_and_rule.Animations;

public class triggeredAnimData extends LoopedAnimData {
    private int stillframe;
    private boolean triggered = false;

    public triggeredAnimData(String atlasPath, String name, float frameDuration, int stillframe) {
        super(atlasPath, name, frameDuration);
        this.stillframe = stillframe;
        this.animIndex = stillframe;
    }

    public void trigger() {
        this.animIndex = 0;
        this.stateTime = 0f;
        this.triggered = true;
    }

    @Override
    public void update(float deltaTime) {
        if (!triggered) {
            return;
        }
        super.update(deltaTime);
    }

    @Override
    public void overflow() {
        this.animIndex = stillframe;
        this.triggered = false;
    }
}
