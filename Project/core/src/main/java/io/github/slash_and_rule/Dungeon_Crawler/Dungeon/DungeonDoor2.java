package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Utils.ColliderObject;

import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

import java.util.function.Consumer;

import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Utils.SensorObject;

public class DungeonDoor2 {
    private boolean isActive = true;
    private boolean isOpen = false;

    private ColliderObject blocker;
    private SensorObject sensor;

    private float[] spawnPos = new float[2];

    private Runnable onEnterCallback;

    public DungeonDoor2(PhysicsScreen screen, RectangleMapObject object, float scale, String type,
            Consumer<String> onEnterCallback) {
        this.onEnterCallback = () -> onEnterCallback.accept(type);

        Rectangle rectangle = object.getRectangle();
        scaleRectangle(rectangle, scale);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(rectangle.width, rectangle.height);

        this.blocker = new ColliderObject(screen, 0, 0, 0, rectangle.x + rectangle.width,
                rectangle.y + rectangle.height,
                Globals.WallCategory, Globals.WallMask, shape1, BodyType.StaticBody);

        calcSpawnpos(rectangle, type);

        makeSensorRectangle(rectangle, type);
        PolygonShape shape2 = new PolygonShape();
        shape2.setAsBox(rectangle.width, rectangle.height);

        this.sensor = new SensorObject(screen, 0, 0, 0, rectangle.x, rectangle.y, Globals.PlayerSensorCategory,
                Globals.PlayerCategory, shape2, type, this::entered);

        setActive(false);
    }

    private void scaleRectangle(Rectangle rectangle, float scale) {
        rectangle.x *= scale;
        rectangle.y *= scale;
        rectangle.width *= scale / 2f;
        rectangle.height *= scale / 2f;
    }

    private void makeSensorRectangle(Rectangle rectangle, String type) {
        if (type.startsWith("r")) {
            rectangle.x += rectangle.width;
        } else if (type.startsWith("t")) {
            rectangle.y += rectangle.height;
        }

        if (type.startsWith("t") || type.startsWith("b")) {
            rectangle.height /= 2f;
        } else {
            rectangle.width /= 2f;
        }
        rectangle.x += rectangle.width;
        rectangle.y += rectangle.height;
    }

    private void calcSpawnpos(Rectangle rectangle, String type) {
        spawnPos[0] = rectangle.x + rectangle.width;
        spawnPos[1] = rectangle.y + rectangle.height;

        if (type.startsWith("t") || type.startsWith("b")) {
            if (type.startsWith("b")) {
                spawnPos[1] += 1;
            } else {
                spawnPos[1] -= 1;
            }
        } else {
            if (!type.startsWith("r")) {
                spawnPos[0] += 1;
            } else {
                spawnPos[0] -= 1;
            }
        }
    }

    private void entered(Fixture fixture) {
        this.onEnterCallback.run();
    }

    public float[] getSpawnPos() {
        return spawnPos;
    }

    public void setOpen(boolean open) {
        if (open == isOpen) {
            return; // No change needed
        }
        isOpen = open;
        Filter filter = blocker.getBody().getFixtureList().first().getFilterData();
        if (isOpen) {
            filter.maskBits = Globals.ItemCategory | Globals.ProjectileCategory;
        } else {
            filter.maskBits = Globals.WallMask;
        }
        blocker.getBody().getFixtureList().first().setFilterData(filter);
        sensor.getBody().setActive(isOpen);
    }

    public void setActive(boolean active) {
        if (active == isActive) {
            return; // No change needed
        }
        isActive = active;
        blocker.getBody().setActive(active);
        sensor.getBody().setActive(active && isOpen);
    }
}
