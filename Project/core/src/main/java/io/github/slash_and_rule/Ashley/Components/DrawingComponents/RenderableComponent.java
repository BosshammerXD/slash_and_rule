package io.github.slash_and_rule.Ashley.Components.DrawingComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class RenderableComponent implements Component {
    public class TextureData implements Comparable<TextureData> {
        public TextureRegion texture;
        public String atlasPath = null;
        public String name = null;

        private int priority;

        public float width = Float.NaN;
        public float height = Float.NaN;
        public float offsetX = Float.NaN;
        public float offsetY = Float.NaN;
        public float angle = 0f;
        public float scale = 1 / 32f;

        public TextureData(int priority) {
            this.priority = priority;
        }

        @Override
        public int compareTo(TextureData other) {
            return Integer.compare(this.priority, other.priority);
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
            dirty = true;
        }

        public TextureData copy() {
            TextureData copy = new TextureData(this.priority);
            copy.texture = this.texture;
            copy.atlasPath = this.atlasPath;
            copy.name = this.name;
            copy.width = this.width;
            copy.height = this.height;
            copy.offsetX = this.offsetX;
            copy.offsetY = this.offsetY;
            copy.angle = this.angle;
            copy.scale = this.scale;
            return copy;
        }
    }

    public boolean dirty = false;

    public TextureData[] textures = new TextureData[0];
}
