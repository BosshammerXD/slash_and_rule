package io.github.slash_and_rule.Utils;

import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AtlasManager {
    private HashMap<String, TextureAtlas> atlases = new HashMap<>();
    private AssetManager assetManager;
    private boolean isLoaded = true;

    public AtlasManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void add(String atlasPath) {
        if (atlases.containsKey(atlasPath)) {
            return;
        }
        assetManager.load(atlasPath, TextureAtlas.class);
        atlases.put(atlasPath, null);
        this.isLoaded = false;
    }

    public void AssetsLoaded() {
        if (isLoaded) {
            return;
        }
        this.isLoaded = true;
        for (String atlasPath : atlases.keySet()) {
            if (atlases.get(atlasPath) != null) {
                continue;
            }
            TextureAtlas atlas = assetManager.get(atlasPath, TextureAtlas.class);
            if (atlas == null) {
                assetManager.load(atlasPath, TextureAtlas.class);
                this.isLoaded = false;
                continue;
            }
            atlases.put(atlasPath, atlas);
        }
    }

    public void remove(String atlasPath) {
        TextureAtlas atlas = atlases.remove(atlasPath);
        if (atlas != null) {
            atlas.dispose();
        }
    }

    public TextureAtlas getAtlas(String atlasPath) {
        TextureAtlas atlas = atlases.get(atlasPath);
        if (atlas == null) {
            throw new IllegalArgumentException("Atlas not found: " + atlasPath);
        }
        return atlas;
    }

    public TextureRegion getTexture(String atlasPath, String name) {
        TextureAtlas atlas = getAtlas(atlasPath);
        TextureRegion region = atlas.findRegion(name);
        if (region == null) {
            throw new IllegalArgumentException("Texture not found: " + name + " in atlas: " + atlasPath);
        }
        return region;
    }

    public TextureRegion[] getAnimation(String atlasPath, String name) {
        TextureAtlas atlas = getAtlas(atlasPath);
        TextureRegion[] frames = atlas.findRegions(name).toArray(TextureRegion.class);
        if (frames.length == 0) {
            throw new IllegalArgumentException("No frames found for animation: " + name + " in atlas: " + atlasPath);
        }
        return frames;
    }
}
