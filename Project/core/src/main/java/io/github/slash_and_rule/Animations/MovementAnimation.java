package io.github.slash_and_rule.Animations;

import java.util.Arrays;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class MovementAnimation {
    public static class AnimData {
        public String name;
        public float frameDuration;

        public AnimData(String name, float frameDuration) {
            this.name = name;
            this.frameDuration = frameDuration;
        }
    }

    private TextureRegion[][] animations;
    private float[] frameDurations;

    private int AnimIndex = 0;
    private float animationTime = 0f;

    /**
     * Constructor for MovementAnimation.
     *
     * @param assetManager   The AssetManager to load animations from.
     * @param atlasPath      The path to the texture atlas containing the
     *                       animations.
     * @param animationNames An array of animation names to load from the atlas.
     */
    public MovementAnimation(AssetManager assetManager, String atlasPath, String[] animationNames,
            float frameDuration) {
        TextureAtlas atlas = assetManager.get(atlasPath, TextureAtlas.class);

        animations = new TextureRegion[animationNames.length][0];
        frameDurations = new float[animationNames.length];
        Arrays.fill(frameDurations, frameDuration);
        int count = 0;
        for (String animationName : animationNames) {
            animations[count++] = atlas.findRegions(animationName).toArray();
        }
    }

    public MovementAnimation(AssetManager assetManager, String atlasPath, AnimData[] animationData) {
        TextureAtlas atlas = assetManager.get(atlasPath, TextureAtlas.class);

        animations = new TextureRegion[animationData.length][0];
        frameDurations = new float[animationData.length];
        int count = 0;
        for (AnimData data : animationData) {
            animations[count] = atlas.findRegions(data.name).toArray();
            frameDurations[count++] = data.frameDuration;
        }
    }

    public void update(Vector2 direction, float delta) {
        if (direction.isZero() || direction.len() < 0.01f) {
            animationTime = 0f; // Reset animation time if no movement
            Index = animations[AnimIndex].length - 1; // Set to last frame
            return;
        }

        int lastDir = AnimIndex;

        float absX = Math.abs(direction.x);
        float absY = Math.abs(direction.y);

        if (absX == absY) {
            if (direction.y > 0) {
                AnimIndex = (lastDir == 1) ? 3 : lastDir;
            } else {
                AnimIndex = (lastDir == 3) ? 1 : lastDir;
            }
        } else if (absX > absY) {
            AnimIndex = (direction.x > 0) ? 2 : 0; // Right or Left
        } else {
            AnimIndex = (direction.y > 0) ? 3 : 1; // Up or Down
        }

        if (lastDir != AnimIndex) {
            animationTime = 0f; // Reset animation time when changing direction
        } else {
            animationTime += delta; // Update animation time
        }
    }

    private int Index;

    public TextureRegion getFrame() {
        if (animationTime >= frameDurations[AnimIndex]) {
            animationTime -= frameDurations[AnimIndex]; // Reset animation time if it exceeds frame duration
            Index++;
        }
        return animations[AnimIndex][Index % animations[AnimIndex].length];
    }

    public int getDir() {
        return AnimIndex;
    }
}
