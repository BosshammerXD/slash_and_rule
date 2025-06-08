package io.github.slash_and_rule.Bases;

import java.util.Stack;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.ColliderObject;
import io.github.slash_and_rule.InputManager;

public abstract class CollidableTileMapObject extends TileMapObject {
    private World world;
    protected PhysicsScreen screen;

    public CollidableTileMapObject(PhysicsScreen screen, InputManager inputManager, World world, String name,
            float scale) {
        super(screen, inputManager, name, scale);
        this.world = world;
        this.screen = screen;
    }

    @Override
    public void init(AssetManager assetManager, Stack<Runnable> todo) {
        todo.push(() -> loadCollisionObjects(assetManager));
        super.init(assetManager, todo);
    };

    private void loadCollisionObjects(AssetManager assetManager) {
        // Load collision objects from the map if necessary
        // This method should be implemented in subclasses to handle specific collision
        // logic
        MapObjects objects = map.getLayers().get("Collision").getObjects();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                RectangleMapObject rectangleObject = (RectangleMapObject) object;
                float x = rectangleObject.getRectangle().x * scale;
                float y = rectangleObject.getRectangle().y * scale;
                float width = rectangleObject.getRectangle().width * scale;
                float height = rectangleObject.getRectangle().height * scale;

                PolygonShape shape = new PolygonShape();
                shape.setAsBox(width / 2f, height / 2f);

                // Create a collider object for the rectangle
                new ColliderObject(screen, inputManager, world, 0f, 0f, 0f, x + width / 2f,
                        y + height / 2f,
                        shape, BodyType.StaticBody);
            }
        }
    }
}
