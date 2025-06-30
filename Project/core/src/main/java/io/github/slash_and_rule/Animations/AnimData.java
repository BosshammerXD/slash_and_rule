package io.github.slash_and_rule.Animations;

public abstract class AnimData {
    public String atlasPath;
    public String name;
    public int animIndex = 0;
    public float stateTime = 0f;

    public AnimData(String atlasPath, String name) {
        this.atlasPath = atlasPath;
        this.name = name;
    }

    public abstract void update(float deltaTime);

    public void overflow() {
        animIndex = 0;
        stateTime = 0f;
    }
}

