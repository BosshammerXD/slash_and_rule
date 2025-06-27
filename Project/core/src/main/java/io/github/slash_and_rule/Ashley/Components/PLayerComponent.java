package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class PlayerComponent implements Component {
    public boolean attackPressed = false;
    public Vector2 mousePos = new Vector2(0, 0);
}
