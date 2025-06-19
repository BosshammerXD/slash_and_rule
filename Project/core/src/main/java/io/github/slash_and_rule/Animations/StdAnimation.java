package io.github.slash_and_rule.Animations;

import java.util.Arrays;
import java.util.function.BiFunction;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Interfaces.Initalizable;

public class StdAnimation extends BaseAnimation implements Initalizable {
    public static class TimeIndex {
        public float time;
        public int index;

        public TimeIndex(float time, int index) {
            this.time = time;
            this.index = index;
        }
    }

    private AnimData data;

    protected BiFunction<TimeIndex, Float, TimeIndex> dataSupplier = null;

    public StdAnimation(BaseScreen screen, AnimData data) {
        screen.loadableObjects.add(this);
        this.data = data;
        if (data.frames == null || data.frames.length == 0) {
            throw new IllegalArgumentException("Animation frames cannot be null or empty");
        }
        if (data.frameDurations == null || data.frameDurations.length != data.frames.length) {
            throw new IllegalArgumentException("Frame durations must match the number of frames");
        }
    }

    public StdAnimation(BaseScreen screen, String name, float[] frameDurations) {
        this(screen, new AnimData(name, frameDurations));
    }

    public StdAnimation(BaseScreen screen, String name, float frameDuration) {
        this(screen, new AnimData(name, frameDuration));
    }

    @Override
    public void init(LoadingScreen loader) {
        loader.loadAsset(data.name, TextureAtlas.class);
        loader.schedule.add(() -> getFrames(loader.getAssetManager()));
    }

    @Override
    public void show(AssetManager assetManager) {
    }

    private void getFrames(AssetManager assetManager) {
        TextureAtlas atlas = assetManager.get(data.name, TextureAtlas.class);
        TextureRegion[] frames = atlas.findRegions(data.name).toArray(TextureRegion.class);
        if (data.frames == null && data.frameDurations != null && data.frameDurations.length > 0) {
            float time = data.frameDurations[0];
            data.frameDurations = new float[frames.length];
            Arrays.fill(data.frameDurations, time);
        }
        data.frames = frames;
        if (data.frames.length != data.frameDurations.length) {
            throw new IllegalArgumentException("Frame durations must match the number of frames");
        }
    }

    public void update(float deltaTime) {
        if (dataSupplier != null) {
            TimeIndex val = new TimeIndex(animationTime, animIndex);
            val = dataSupplier.apply(val, deltaTime);
            animationTime = val.time;
            animIndex = val.index;
        } else {
            animationTime += deltaTime;
            while (animationTime >= data.frameDurations[animIndex]) {
                animationTime -= data.frameDurations[animIndex];
                animIndex++;
                if (animIndex >= data.frames.length) {
                    animIndex = 0;
                }
            }
        }
    }

    public TextureRegion getFrame() {
        return data.frames[animIndex];
    };

    @Override
    public void dispose() {
        TextureRegion[] frames = data.frames;
        if (frames == null) {
            return; // Nothing to dispose
        }
        for (TextureRegion frame : frames) {
            if (frame != null && frame.getTexture() != null) {
                frame.getTexture().dispose(); // Dispose of the texture
            }
        }
    }

    public void setDataSupplier(BiFunction<TimeIndex, Float, TimeIndex> deltaTimeSupplier) {
        this.dataSupplier = deltaTimeSupplier;
    }
}
