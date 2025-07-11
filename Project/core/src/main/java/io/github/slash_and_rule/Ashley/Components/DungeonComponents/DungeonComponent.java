package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.slash_and_rule.Utils.QuadData;

public class DungeonComponent implements Component {

    public QuadData<Fixture[]> doors;
    public QuadData<Vector2> spawnPoints = new QuadData<>();
    public Vector2[] spawnerPositions;
}
