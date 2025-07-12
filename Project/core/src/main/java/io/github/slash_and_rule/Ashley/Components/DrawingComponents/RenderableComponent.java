package io.github.slash_and_rule.Ashley.Components.DrawingComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RenderableComponent implements Component {
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
    }

    public boolean dirty = false;

    public TextureData[] textures = new TextureData[0];
}
