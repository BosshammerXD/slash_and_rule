package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.Stack;
import java.util.function.Consumer;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.ColliderData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.DoorData;
import io.github.slash_and_rule.Utils.ColliderObject;
import io.github.slash_and_rule.Utils.SensorObject;

public class RoomDataHandler implements Disposable {
    public static class DungeonDoor {
        private ColliderObject blocker;
        private SensorObject sensor;

        private float[] spawnPos = new float[2];

        public boolean isActive = true;
        public boolean isOpen = false;

        public DungeonDoor(ColliderObject blocker, SensorObject sensor, float[] spawnPos) {
            this.blocker = blocker;
            this.sensor = sensor;
            this.spawnPos = spawnPos;
        }

        public void setActive(boolean active) {
            if (this.isActive == active) {
                return; // No change needed
            }
            this.isActive = active;
            this.blocker.getBody().setActive(active);
            this.sensor.getBody().setActive(active && this.isOpen);
        }

        public void setOpen(boolean open) {
            if (this.isOpen == open) {
                return; // No change needed
            }
            this.isOpen = open;
            this.sensor.getBody().setActive(this.isActive && open);
            Filter filter = this.blocker.getBody().getFixtureList().get(0).getFilterData();
            filter.maskBits = open ? Globals.ProjectileCategory | Globals.ItemCategory : Globals.WallMask;
            this.blocker.getBody().getFixtureList().get(0).setFilterData(filter);
        }

        public float[] getSpawnPos() {
            return spawnPos;
        }
    }

    public TiledMap map = null;

    public ColliderObject[] walls = null;
    public DungeonDoor[] doors = null;

    public boolean isActive = false;
    public boolean isOpen = false;

    private PhysicsScreen screen;
    private Consumer<Integer> onRoomChange;

    private Stack<ColliderObject> wallPool = new Stack<>();
    private ColliderObject blockerHolder;
    private SensorObject sensorHolder;

    public RoomDataHandler(PhysicsScreen screen, Consumer<Integer> onRoomChange) {
        this.screen = screen;
        this.onRoomChange = onRoomChange;

        screen.disposableObjects.add(this);
    }

    public RoomDataHandler(PhysicsScreen screen, Consumer<Integer> onRoomChange, boolean isActive, boolean isOpen) {
        this.screen = screen;
        this.onRoomChange = onRoomChange;
        this.isActive = isActive;
        this.isOpen = isOpen;

        screen.disposableObjects.add(this);
    }

    @Override
    public void dispose() {
        if (walls != null) {
            for (ColliderObject wall : walls) {
                if (wall != null) {
                    wall.dispose();
                }
            }
        }
        if (doors != null) {
            for (DungeonDoor door : doors) {
                if (door != null) {
                    door.blocker.dispose();
                    door.sensor.dispose();
                }
            }
        }
        this.map = null;
        this.walls = null;
        this.doors = null;
        this.isActive = false;
        this.isOpen = false;
    }

    public void setActive(boolean active) {
        if (this.isActive == active) {
            return; // No change needed
        }
        forceActive(active);
    }

    private void forceActive(boolean active) {
        this.isActive = active;
        if (map == null || walls == null || doors == null) {
            return; // No walls or doors to activate
        }
        for (ColliderObject wall : walls) {
            wall.getBody().setActive(active);
        }
        for (DungeonDoor door : doors) {
            if (door != null) {
                door.setActive(active);
            }
        }
    }

    public void setOpenSide(boolean open, int direction) {

    }

    public void setOpen(boolean open) {
        if (this.isOpen == open) {
            return; // No change needed
        }
        forceOpen(open);
    }

    private void forceOpen(boolean open) {
        this.isOpen = open;
        if (map == null || walls == null || doors == null) {
            return; // No walls or doors to open
        }
        for (DungeonDoor door : doors) {
            if (door != null) {
                door.setOpen(open);
            }
        }
    }

    private void notifyRoomChange(String doorType) {
        if (onRoomChange != null) {
            onRoomChange.accept(doorTypeToDirection(doorType));
        }
    }

    private int doorTypeToDirection(String doorType) {
        switch (doorType) {
            case "left":
                return 0;
            case "right":
                return 2;
            case "top":
                return 3;
            case "bottom":
                return 1;
            default:
                throw new IllegalArgumentException("Invalid door type: " + doorType);
        }
    }

    public void clear() {
        this.map = null;
        this.walls = null;
        this.doors = null;
        this.isActive = false;
        this.isOpen = false;
    }

    public void loadRoomData(RoomData data, DungeonRoom room) {
        this.map = data.map;
        loadWalls(data);
        loadDoors(data, room);
        screen.schedule.add(() -> {
            this.forceActive(this.isActive);
            this.forceOpen(this.isOpen);
        });
    }

    public void loadRoomData(RoomData data, DungeonRoom room, boolean isOpen, boolean isActive) {
        loadRoomData(data, room);
        screen.schedule.add(() -> {
            this.setActive(isActive);
            this.setOpen(isOpen);
        });
    }

    private void loadWalls(RoomData data) {
        this.walls = new ColliderObject[data.walls.length];
        int index = 0;
        for (ColliderData wallData : data.walls) {
            final int wallIndex = index; // Final variable for lambda
            screen.schedule.add(() -> loadWall(wallData, wallIndex));
            index++;
        }
    }

    private void loadWall(ColliderData wallData, int index) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(wallData.width, wallData.height);
        this.walls[index] = new ColliderObject(screen, 0, 0, 0, wallData.x, wallData.y, Globals.WallCategory,
                Globals.WallMask, shape, BodyType.StaticBody, false);
    }

    private void loadDoors(RoomData data, DungeonRoom room) {
        this.doors = new DungeonDoor[4];
        for (DoorData doorData : data.doors) {
            loadDoor(doorData, room);
        }
        screen.schedule.add(this::extendWalls);
    }

    private void loadDoor(DoorData data, DungeonRoom room) {
        int direction = doorTypeToDirection(data.type);
        if (room.neighbours[direction] == null) {
            screen.schedule.add(() -> {
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(data.collider.width, data.collider.height);
                wallPool.add(new ColliderObject(screen, 0, 0, 0, data.collider.x, data.collider.y, Globals.WallCategory,
                        Globals.WallMask, shape, BodyType.StaticBody, false));
            });
            return;
        }
        screen.schedule.add(() -> {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(data.collider.width, data.collider.height);
            this.blockerHolder = new ColliderObject(screen, 0, 0, 0,
                    data.collider.x, data.collider.y, Globals.WallMask, Globals.WallCategory,
                    shape, BodyType.StaticBody, false);
        });
        screen.schedule.add(() -> {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(data.sensor.width, data.sensor.height);
            this.sensorHolder = new SensorObject(screen, 0, 0, 0,
                    data.sensor.x, data.sensor.y, Globals.PlayerSensorCategory, Globals.PlayerCategory,
                    shape, data.type, false, fixture -> notifyRoomChange(data.type));
        });
        screen.schedule.add(() -> {
            this.doors[direction] = new DungeonDoor(this.blockerHolder, this.sensorHolder, data.spawnPoint);
        });
    }

    private void extendWalls() {
        if (wallPool.isEmpty()) {
            return; // No walls to extend
        }
        ColliderObject[] oldWalls = this.walls;
        this.walls = new ColliderObject[oldWalls.length + wallPool.size()];
        System.arraycopy(oldWalls, 0, this.walls, 0, oldWalls.length);
        for (int i = oldWalls.length; i < this.walls.length; i++) {
            this.walls[i] = wallPool.pop();
        }
    }
}