package io.github.slash_and_rule.Ashley.Components.DrawingComponents;

import java.util.ArrayDeque;
import java.util.TreeMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.slash_and_rule.Animations.AnimData;

public class RenderableComponent implements Component {
    public static class TextureData {
        public TextureRegion texture;
        public AnimData animData;

        public float width;
        public float height;
        public float offsetX;
        public float offsetY;
        public float angle = 0f;

        public void scale(float scale) {
            this.width *= scale;
            this.height *= scale;
        }
    }

    public TreeMap<Integer, ArrayDeque<TextureData>> textures = new TreeMap<>();

    public void addTextureDatas(int priority, TextureData... textureDatas) {
        ArrayDeque<TextureData> textureDataQueue = textures.get(priority);
        if (textureDataQueue == null) {
            textureDataQueue = new ArrayDeque<>();
            textures.put(priority, textureDataQueue);
        }
        for (TextureData textureData : textureDatas) {
            textureDataQueue.add(textureData);
        }
    }

    public void removeTextureDatas(int priority, TextureData... textureDatas) {
        ArrayDeque<TextureData> textureDataQueue = textures.get(priority);
        if (textureDataQueue != null) {
            for (TextureData textureData : textureDatas) {
                textureDataQueue.remove(textureData);
            }
            if (textureDataQueue.isEmpty()) {
                textures.remove(priority);
            }
        }
    }

    public void changePriority(int oldPriority, int newPriority, TextureData textureData) {
        ArrayDeque<TextureData> oldQueue = textures.get(oldPriority);
        if (oldQueue != null && oldQueue.remove(textureData)) {
            ArrayDeque<TextureData> newQueue = textures.get(newPriority);
            if (newQueue == null) {
                newQueue = new ArrayDeque<>();
                textures.put(newPriority, newQueue);
            }
            newQueue.add(textureData);
            if (oldQueue.isEmpty()) {
                textures.remove(oldPriority);
            }
        }
    }

    public ArrayDeque<TextureData> getAllTextures() {
        ArrayDeque<TextureData> allTextures = new ArrayDeque<>();
        for (ArrayDeque<TextureData> textureDataQueue : textures.values()) {
            allTextures.addAll(textureDataQueue);
        }
        return allTextures;
    }
}
