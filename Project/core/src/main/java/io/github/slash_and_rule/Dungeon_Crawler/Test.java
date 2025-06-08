package io.github.slash_and_rule.Dungeon_Crawler;

import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.Bases.CollidableTileMapObject;
import io.github.slash_and_rule.Bases.PhysicsScreen;

public class Test extends CollidableTileMapObject {
    private static final String MAP_FILE_PATH = "test.tmx";
    private static final float SCALE = 1 / 16f;

    public Test(PhysicsScreen screen, InputManager inputManager, World world) {
        super(screen, inputManager, world, MAP_FILE_PATH, SCALE);
    }
}
