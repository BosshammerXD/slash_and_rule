package io.github.slash_and_rule.Animations;

import java.util.function.BiFunction;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.slash_and_rule.Animations.StdAnimation.TimeIndex;
import io.github.slash_and_rule.Bases.BaseScreen;

public class ToggleableAnimation extends BaseAnimation {
    public static class TimeTypeIndex {
        public float time;
        public int animIndex;
        public int typeIndex;

        public TimeTypeIndex(float time, int animIndex, int typeIndex) {
            this.time = time;
            this.animIndex = animIndex;
            this.typeIndex = typeIndex;
        }
    }

    private StdAnimation[] animations;

    protected BiFunction<TimeTypeIndex, Float, TimeTypeIndex> dataSupplier = null;

    private int typeIndex = 0;

    public ToggleableAnimation(BaseScreen screen, AnimData[] animDatas,
            BiFunction<TimeTypeIndex, Float, TimeTypeIndex> dataSupplier) {
        animations = new StdAnimation[animDatas.length];
        for (int i = 0; i < animDatas.length; i++) {
            animations[i] = new StdAnimation(screen, animDatas[i]);
            animations[i].setDataSupplier(this::supplierFunc);
        }
        this.dataSupplier = dataSupplier;
    }

    private TimeIndex supplierFunc(TimeIndex timeIndex, float deltaTime) {
        return new TimeIndex(animationTime, animIndex);
    }

    @Override
    public void update(float deltaTime) {
        if (dataSupplier == null) {
            throw new IllegalStateException("Data supplier function is not set.");
        }
        TimeTypeIndex timeTypeIndex = new TimeTypeIndex(animationTime, animIndex, typeIndex);
        timeTypeIndex = dataSupplier.apply(timeTypeIndex, deltaTime);
        animationTime = timeTypeIndex.time;
        animIndex = timeTypeIndex.animIndex;
        typeIndex = timeTypeIndex.typeIndex;
    }

    @Override
    public TextureRegion getFrame() {
        return animations[typeIndex].getFrame();
    }

    @Override
    public void dispose() {
        for (BaseAnimation anim : animations) {
            if (anim != null) {
                anim.dispose();
            }
        }
    }

    public void setDataSupplier(BiFunction<TimeTypeIndex, Float, TimeTypeIndex> deltaTimeSupplier) {
        this.dataSupplier = deltaTimeSupplier;
    }
}
