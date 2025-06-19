package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class TransformComponent implements Component {
    public Vector2 pos = new Vector2();
    public Vector2 velocity = new Vector2(0, 0);
    public float rotation = 0f;

    // if in middfield z is used like an offset (to y pos) => zIndex = y + z
    // if in foreground or background z is used like a zIndex => zIndex = z
    public float z = 0f;
}
