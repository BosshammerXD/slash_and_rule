package io.github.slash_and_rule.Animations;

public class LoopedAnimData extends AnimData {
    protected float frameDuration;

    public LoopedAnimData(String atlasPath, String name, float frameDuration) {
        super(atlasPath, name);
        this.frameDuration = frameDuration;
    }

    @Override
    public void update(float deltaTime) {
        stateTime += deltaTime;
        while (stateTime >= frameDuration) {
            animIndex++;
            stateTime -= frameDuration;
        }
        if (animIndex < 0) {
            animIndex = 0;
        }
    }
}
