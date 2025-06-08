package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.Bases.CollidableTileMapObject;
import io.github.slash_and_rule.Bases.PhysicsScreen;

public class DungeonTileMap extends CollidableTileMapObject {
    public DungeonTileMap(PhysicsScreen screen, InputManager inputManager, World world, String mapPath) {
        super(screen, inputManager, world, mapPath, 1 / 16f);
    }

    @Override
    protected MapLayers loadCollisionObjects(AssetManager assetManager) {
        MapLayers layers = super.loadCollisionObjects(assetManager);

        MapObjects objects = layers.get("door").getObjects();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                String type = object.getProperties().get("type", String.class);

                new DungeonDoor(screen, inputManager, world, (RectangleMapObject) object, scale, type);
            }
        }

        return layers;

    }
}
