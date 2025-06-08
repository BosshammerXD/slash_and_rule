package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Utils.ColliderObject;

import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

import java.util.function.Consumer;

import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.Utils.SensorObject;

public class DungeonDoor {
    private boolean isOpen = false;
    private ColliderObject blocker;
    private SensorObject sensor;

    public DungeonDoor(PhysicsScreen screen, InputManager inputManager, World world, RectangleMapObject object,
            float scale, String type) {

        makeDoor(screen, inputManager, world, object, scale, type);
    }

    private void makeDoor(PhysicsScreen screen, InputManager inputManager, World world, RectangleMapObject object,
            float scale, String type) {
        float x = object.getRectangle().x * scale;
        float y = object.getRectangle().y * scale;
        float width = object.getRectangle().width / 2 * scale;
        float height = object.getRectangle().height / 2 * scale;

        makeBlocker(screen, inputManager, world, x, y, width, height);

        boolean isVertical = type.startsWith("t") || type.startsWith("b");
        boolean isFlipped = type.startsWith("b") || type.startsWith("r");

        if (isVertical) {
            if (!isFlipped) {
                y += height;
            }
            height /= 2;
        } else {
            if (isFlipped) {
                x += width;
            }
            width /= 2;

        }
        x += 1 / 16f;
        y += 1 / 16f;
        width -= 1 / 16f;
        height -= 1 / 16f;

        makeSensor(screen, inputManager, world, x, y, width, height, type);
    }

    private void makeBlocker(PhysicsScreen screen, InputManager inputManager, World world, float x, float y,
            float width,
            float height) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width, height);

        this.blocker = new ColliderObject(screen, inputManager, world, 0, 0, 0, x + width, y + height,
                Globals.WallCategory,
                Globals.WallMask, shape, BodyType.StaticBody);
    }

    private void makeSensor(PhysicsScreen screen, InputManager inputManager, World world, float x, float y,
            float width,
            float height, String type) {

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width, height);

        Consumer<Fixture> contactHandler = fixture -> {
            System.out.println("Sensor " + type + " triggered by: " + fixture.getUserData());
        };

        this.sensor = new SensorObject(screen, inputManager, world, 0, 0, 0, x + width, y + height,
                Globals.PlayerSensorCategory, Globals.PlayerCategory, shape, type, contactHandler);
    }

    public void open() {
        if (isOpen)
            return;

        isOpen = true;
        Filter filter = blocker.getBody().getFixtureList().first().getFilterData();
        filter.maskBits = Globals.ItemCategory | Globals.ProjectileCategory;
        blocker.getBody().getFixtureList().first().setFilterData(filter);
    }

    public void close() {
        if (!isOpen)
            return;

        isOpen = false;
        Filter filter = blocker.getBody().getFixtureList().first().getFilterData();
        filter.maskBits = Globals.WallMask;
        blocker.getBody().getFixtureList().first().setFilterData(filter);
    }

}
