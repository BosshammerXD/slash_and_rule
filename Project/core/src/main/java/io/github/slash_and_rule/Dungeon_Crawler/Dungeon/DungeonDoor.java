package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import io.github.slash_and_rule.Bases.PhysicsScreen;

import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.ColliderObject;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.InputManager;

public class DungeonDoor {
    private boolean isVertical;
    private boolean isFlipped;
    private boolean isOpen = true;
    private ColliderObject blocker;

    public DungeonDoor(PhysicsScreen screen, InputManager inputManager, World world, RectangleMapObject object,
            float scale, String type) {
        this.isVertical = type.startsWith("t") || type.startsWith("b");
        this.isFlipped = type.startsWith("b") || type.startsWith("r");

        make_blocker(screen, inputManager, world, object, scale);
    }

    private void make_blocker(PhysicsScreen screen, InputManager inputManager, World world, RectangleMapObject object,
            float scale) {
        float x = object.getRectangle().x * scale;
        float y = object.getRectangle().y * scale;
        float width = object.getRectangle().width / 2 * scale;
        float height = object.getRectangle().height / 2 * scale;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width, height);

        this.blocker = new ColliderObject(screen, inputManager, world, 0, 0, 0, x + width, y + height,
                Globals.WallCategory,
                Globals.WallMask, shape, null);
    }

}
