package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class TransformComponent implements Component {
    public Vector2 position;
    public Vector2 lastPosition;
    public float rotation = 0f;

    // if in middfield z is used like an offset (to y pos) => zIndex = y + z
    // if in foreground or background z is used like a zIndex => zIndex = z
    public float z = 0f;

    public TransformComponent(Vector2 position, float rotation, float z) {
        this.position = position;
        this.lastPosition = new Vector2().set(position);
        this.rotation = rotation;
        this.z = z;
    }

    public TransformComponent(Vector2 position, float rotation) {
        this(position, rotation, 0f);
    }

    public TransformComponent() {
        this(new Vector2(0f, 0f), 0f, 0f);
    }
}
