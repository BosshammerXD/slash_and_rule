package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

public class DungeonComponent implements Component {
    public TiledMap map;
    public Vector2[] spawnPoints = new Vector2[4]; // 0: left, 1: down, 2: right, 3: up
    public Fixture[][] doors = new Fixture[4][]; // 0: left, 1: down, 2: right, 3: up
}
