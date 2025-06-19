package io.github.slash_and_rule.Animations;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

public abstract class BaseAnimation implements Disposable {
    public static class AnimData {
        public String name;
        public TextureRegion[] frames = null;
        public float[] frameDurations;

        public AnimData(String name, float[] frameDuration) {
            this.name = name;
            this.frames = new TextureRegion[1];
            this.frameDurations = frameDuration;
        }

        public AnimData(String name, float frameDuration) {
            this.name = name;
            this.frameDurations = new float[] { frameDuration };
        }
    }

    protected int animIndex = 0;

    protected float animationTime = 0f;

    public abstract void update(float deltaTime);

    public abstract TextureRegion getFrame();
}
