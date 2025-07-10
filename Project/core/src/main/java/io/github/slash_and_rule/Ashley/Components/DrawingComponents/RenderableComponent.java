package io.github.slash_and_rule.Ashley.Components.DrawingComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RenderableComponent implements Component {
    public class TextureData implements Comparable<TextureData> {
        public TextureRegion texture;
        public String atlasPath = null;
        public String name = null;

        private int priority;

        public float width;
        public float height;
        public float offsetX;
        public float offsetY;
        public float angle = 0f;

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
