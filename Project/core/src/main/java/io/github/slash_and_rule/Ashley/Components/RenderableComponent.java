package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RenderableComponent implements Component {
    public static class TextureData {
        public TextureRegion texture;
        public float width;
        public float height;
        public float offsetX;
        public float offsetY;

        public TextureData(TextureRegion texture, float width, float height, float offsetX, float offsetY) {
            this.texture = texture;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    public TextureData[] textures;
}
