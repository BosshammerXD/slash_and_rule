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
import io.github.slash_and_rule.Dungeon_Crawler.Player;
import io.github.slash_and_rule.LoadingScreen.LoadingSchedule;
import io.github.slash_and_rule.LoadingScreen.MsgRunnable;
import io.github.slash_and_rule.Utils.ColliderObject;

public class DungeonTileMap extends CollidableTileMapObject {
    private DungeonDoor leftDoor = null;
    private DungeonDoor rightDoor = null;
    private DungeonDoor topDoor = null;
    private DungeonDoor bottomDoor = null;
    private boolean isActive = true;
    private Player player;

    public DungeonTileMap(PhysicsScreen screen, InputManager inputManager, World world, String mapPath, Player player) {
        super(screen, inputManager, world, mapPath, 1 / 16f);
        this.player = player;
    }

    @Override
    protected MapLayers loadCollisionObjects(AssetManager assetManager) {
        MapLayers layers = super.loadCollisionObjects(assetManager);

        MapObjects objects = layers.get("door").getObjects();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                String type = object.getProperties().get("type", String.class);

                DungeonDoor door = new DungeonDoor(screen, inputManager, world, (RectangleMapObject) object, scale,
                        type, player);

                if (type.startsWith("l")) {
                    leftDoor = door;
                } else if (type.startsWith("r")) {
                    rightDoor = door;
                } else if (type.startsWith("t")) {
                    topDoor = door;
                } else if (type.startsWith("b")) {
                    bottomDoor = door;
                }
            }
        }

        return layers;

    }

    @Override
    public void init(LoadingSchedule loader) {
        // TODO Auto-generated method stub
        super.init(loader);

        loader.todo.add(new MsgRunnable("", () -> deactivate()));
    }

    public DungeonDoor getLeftDoor() {
        return leftDoor;
    }

    public DungeonDoor getRightDoor() {
        return rightDoor;
    }

    public DungeonDoor getTopDoor() {
        return topDoor;
    }

    public DungeonDoor getBottomDoor() {
        return bottomDoor;
    }

    public void activate() {
        if (isActive) {
            return; // Already active, no need to activate again
        }
        screen.drawableObjects.add(this);
        for (ColliderObject collider : colliders) {
            collider.getBody().setActive(true);
        }
        if (leftDoor != null) {
            leftDoor.activate();
        }
        if (rightDoor != null) {
            rightDoor.activate();
        }
        if (topDoor != null) {
            topDoor.activate();
        }
        if (bottomDoor != null) {
            bottomDoor.activate();
        }
        isActive = true;
    }

    public void deactivate() {
        if (!isActive) {
            return; // Already inactive, no need to deactivate again
        }
        screen.drawableObjects.remove(this);
        for (ColliderObject collider : colliders) {
            collider.getBody().setActive(false);
        }
        if (leftDoor != null) {
            System.out.println("Deactivating left door");
            leftDoor.deactivate();
        }
        if (rightDoor != null) {
            rightDoor.deactivate();
        }
        if (topDoor != null) {
            topDoor.deactivate();
        }
        if (bottomDoor != null) {
            bottomDoor.deactivate();
        }
        isActive = false;
    }

    public void setDoors(boolean state) {
        if (leftDoor != null) {
            leftDoor.setOpen(state);
        }
        if (rightDoor != null) {
            rightDoor.setOpen(state);
        }
        if (topDoor != null) {
            topDoor.setOpen(state);
        }
        if (bottomDoor != null) {
            bottomDoor.setOpen(state);
        }
    }
}
