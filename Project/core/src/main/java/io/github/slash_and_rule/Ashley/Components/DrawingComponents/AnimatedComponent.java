package io.github.slash_and_rule.Ashley.Components.DrawingComponents;

import java.util.HashMap;

import com.badlogic.ashley.core.Component;

import io.github.slash_and_rule.Animations.AnimData;

public class AnimatedComponent implements Component {
    public HashMap<String, AnimData> animations = new HashMap<>();
}
