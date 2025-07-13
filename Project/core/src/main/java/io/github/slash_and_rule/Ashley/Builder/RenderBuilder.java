package io.github.slash_and_rule.Ashley.Builder;

import java.util.ArrayDeque;
import java.util.Arrays;

import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Bases.BaseCompBuilder;

public class RenderBuilder<T extends RenderableComponent> extends BaseCompBuilder<T> {
    private ArrayDeque<TextureData> textureDataQueue = new ArrayDeque<>();

    public void begin(T comp) {
        super.begin(comp);
    }

    public TextureData add(String atlasPath, String name, int priority, float width, float height,
            float offsetX, float offsetY) {
        checkBuilding();
        TextureData textureData = comp.new TextureData(priority);

        textureData.atlasPath = atlasPath;
        textureData.name = name;
        textureData.width = width;
        textureData.height = height;
        textureData.offsetX = offsetX;
        textureData.offsetY = offsetY;

        textureDataQueue.add(textureData);
        return textureData;
    }

    public void add(TextureData textureData) {
        textureDataQueue.add(textureData);
    }

    public TextureData add(String atlasPath, int priority, float width, float height,
            float offsetX, float offsetY) {
        return add(atlasPath, null, priority, width, height, offsetX, offsetY);
    }

    public TextureData add(String atlasPath, int priority, float scale) {
        return add(atlasPath, null, priority, scale);
    }

    public TextureData add(String atlasPath, String name, int priority, float scale) {
        TextureData textureData = comp.new TextureData(priority);
        textureData.atlasPath = atlasPath;
        textureData.name = name;
        textureData.scale = scale;

        textureDataQueue.add(textureData);
        return textureData;
    }

    @Override
    protected void finish() {
        comp.textures = textureDataQueue.toArray(new TextureData[0]);
        Arrays.sort(comp.textures);
        textureDataQueue.clear();
    }
}
