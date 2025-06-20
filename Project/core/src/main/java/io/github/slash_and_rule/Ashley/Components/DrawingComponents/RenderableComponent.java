package io.github.slash_and_rule.Ashley.Components.DrawingComponents;

import java.util.function.BiConsumer;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RenderableComponent implements Component {
    public static class AnimData {
        public String atlasPath;
        public String name;
        public int animIndex = 0;
        public float stateTime = 0f;
        public BiConsumer<AnimData, Float> updater;

        public AnimData(String atlasPath, String name, BiConsumer<AnimData, Float> updater) {
            this.atlasPath = atlasPath;
            this.name = name;
            this.updater = updater;
        }

        public AnimData(String atlasPath, String name, BiConsumer<AnimData, Float> updater, float stateTime) {
            this(atlasPath, name, updater);
            this.stateTime = stateTime;
        }
    }

    public static class TextureData {
        public TextureRegion texture;
        public AnimData animData;
        public int priority;

        public float width;
        public float height;
        public float offsetX;
        public float offsetY;

        public void scale(float scale) {
            this.width *= scale;
            this.height *= scale;
        }
    }

    public TextureData[] textures;
}
