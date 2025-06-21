package io.github.slash_and_rule.Animations;

import java.util.HashMap;
import java.util.function.BiConsumer;

import com.badlogic.gdx.math.Vector2;

import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.AnimData;

public class AnimationFunctions {
    public static class NameTime {
        public String name;
        public float time;

        public NameTime(String name, float time) {
            this.name = name;
            this.time = time;
        }
    }

    public static NameTime[] makeNameTimes(String[] names, float[] times) {
        if (names.length != times.length) {
            throw new IllegalArgumentException("Names and times arrays must have the same length.");
        }
        NameTime[] nameTimes = new NameTime[names.length];
        for (int i = 0; i < names.length; i++) {
            nameTimes[i] = new NameTime(names[i], times[i]);
        }
        return nameTimes;
    }

    public static BiConsumer<AnimData, Float> mappedTimes(NameTime[] nameTimes, float defaultTime) {
        HashMap<String, Float> timeMap = new HashMap<>();
        for (NameTime nt : nameTimes) {
            timeMap.put(nt.name, nt.time);
        }
        return (animData, delta) -> {
            float time;
            animData.stateTime += delta;
            if (animData.name == null || !timeMap.containsKey(animData.name)) {
                time = defaultTime;
            } else {
                time = timeMap.get(animData.name);
            }
            while (animData.stateTime >= time) {
                animData.stateTime -= time;
                animData.animIndex++;
            }
        };
    }

    public static void movementAnimData(AnimData animData, Vector2 direction, String[] animNames, float delta,
            float margin) {
        float range = margin * delta;
        if (direction.isZero() || direction.len() < 0.01f) {
            animData.stateTime = 0f; // Reset animation time if no movement
            animData.animIndex = 0;
        }

        String lastDir = animData.name;

        float absX = Math.abs(direction.x);
        float absY = Math.abs(direction.y);

        if (Math.abs(absX - absY) < range) {
            if (direction.y > range) {
                animData.name = (lastDir.equals(animNames[1])) ? animNames[3] : lastDir;
            } else if (direction.y < -range) {
                animData.name = (lastDir.equals(animNames[3])) ? animNames[1] : lastDir;
            }
        } else if (absX > absY) {
            animData.name = (direction.x > 0) ? animNames[2] : animNames[0]; // Right or Left
        } else {
            animData.name = (direction.y > 0) ? animNames[3] : animNames[1]; // Up or Down
        }

        if (!lastDir.equals(animData.name)) {
            animData.stateTime = 0f; // Reset animation time when changing direction
        } else {
            animData.stateTime += delta; // Update animation time
        }
    }
}
