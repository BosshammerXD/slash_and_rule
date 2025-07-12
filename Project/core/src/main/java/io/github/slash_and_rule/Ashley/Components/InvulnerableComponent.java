package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;

public class InvulnerableComponent implements Component {
    float duration = 0f;

    public InvulnerableComponent(float duration) {
        this.duration = duration;
    }

    public InvulnerableComponent() {
        this(0f);
    }
}
