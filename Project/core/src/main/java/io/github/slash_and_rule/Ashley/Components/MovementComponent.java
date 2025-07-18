package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class MovementComponent implements Component {
    public Vector2 knockback = new Vector2(0f, 0f);
    public Vector2 velocity = new Vector2(0f, 0f);
    public float max_speed;

    public MovementComponent() {
        this(new Vector2(0f, 0f), 10f);
    }

    public MovementComponent(Vector2 velocity, float max_speed) {
        this.velocity.set(velocity);
        this.max_speed = max_speed;
    }
}
