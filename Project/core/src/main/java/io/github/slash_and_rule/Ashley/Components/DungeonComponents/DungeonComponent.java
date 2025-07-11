package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.slash_and_rule.Utils.QuadData;

public class DungeonComponent implements Component {
    public static enum State {
        INACTIVE, // Room is not active
        ACTIVATE, // Room is scheduled for activation
        ACTIVE, // Room is active
        DEACTIVATE // Room is scheduled for deactivation
    }

    public State active = State.INACTIVE; // 0: inactive, 1: scheduled for activation, 2: active

    public QuadData<Fixture[]> doors;
    public QuadData<Vector2> spawnPoints = new QuadData<>();
}
