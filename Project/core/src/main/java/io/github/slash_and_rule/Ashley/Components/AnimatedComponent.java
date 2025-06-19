package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;

import io.github.slash_and_rule.Animations.BaseAnimation;

public class AnimatedComponent implements Component {
    public static class AnimationData {
        public int textureIndex;
        public BaseAnimation animation;

        public AnimationData(int textureIndex, BaseAnimation animation) {
            this.textureIndex = textureIndex;
            this.animation = animation;
        }
    }

    public AnimationData[] animations;
}
