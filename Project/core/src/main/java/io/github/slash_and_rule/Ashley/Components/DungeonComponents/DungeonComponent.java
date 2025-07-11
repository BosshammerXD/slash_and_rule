package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

import io.github.slash_and_rule.Utils.QuadData;

public class DungeonComponent implements Component {
    public int balance;
    public TiledMap map;
    public boolean cleared = false;
    public QuadData<Vector2> spawnPoints;
    public Vector2[] spawnerPositions;
}
