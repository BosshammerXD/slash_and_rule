package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class TransformComponent implements Component {
    public Vector2 position = new Vector2();
    public Vector2 lastPosition = new Vector2().set(position);
    public float rotation = 0f;

    // if in middfield z is used like an offset (to y pos) => zIndex = y + z
    // if in foreground or background z is used like a zIndex => zIndex = z
    public float z = 0f;
}
