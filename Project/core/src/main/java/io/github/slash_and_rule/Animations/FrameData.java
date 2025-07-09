package io.github.slash_and_rule.Animations;

import java.util.Arrays;

public class FrameData {
    private String name;
    private float[] frameDurations;

    public FrameData(int numFrames, float initialValue, String name) {
        frameDurations = new float[numFrames];
        Arrays.fill(frameDurations, initialValue);
        this.name = name;
    }

    public float[] getFrames() {
        return frameDurations;
    }

    public float get(int index) {
        if (index < 0 || index >= frameDurations.length) {
            throw new IndexOutOfBoundsException("Invalid frame index");
        }
        return frameDurations[index];
    }

    public int length() {
        return frameDurations.length;
    }

    public String getName() {
        return name;
    }


    public void mult(float factor) {
        for (int i = 0; i < frameDurations.length; i++) {
            frameDurations[i] *= factor;
        }
    }

    public void setAll(float value) {
        Arrays.fill(frameDurations, value);
    }

    public void setIndex(int index, float value) {
        if (index < 0 || index >= frameDurations.length) {
            throw new IndexOutOfBoundsException("Invalid frame index");
        }
        frameDurations[index] = value;
    }

    public static FrameData[] createMultiple(int[] numFrames, String[] names, float initialValue) {
        if (numFrames.length != names.length) {
            throw new IllegalArgumentException("numFrames and names arrays must have the same length");
        }
        FrameData[] frameDataArray = new FrameData[numFrames.length];
        for (int i = 0; i < numFrames.length; i++) {
            frameDataArray[i] = new FrameData(numFrames[i], initialValue, names[i]);
        }
        return frameDataArray;
    }
}