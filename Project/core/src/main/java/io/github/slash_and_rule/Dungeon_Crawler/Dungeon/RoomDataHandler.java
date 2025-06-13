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
import io.github.slash_and_rule.Utils.Generation;
import io.github.slash_and_rule.Utils.SensorObject;

public class RoomDataHandler implements Disposable {
    public static class DungeonDoor {
        private ColliderObject blocker;
        private SensorObject sensor;

        private float[] spawnPos = new float[2];

        public boolean isActive = true;
        public boolean isOpen = false;
        public boolean isLocked = true;

        public DungeonDoor(ColliderObject blocker, SensorObject sensor, float[] spawnPos) {
            this.blocker = blocker;
            this.sensor = sensor;
            this.spawnPos = spawnPos;
            this.forceOpen(false);
        }

        public void unlock() {
            this.isLocked = false;
            System.out.println("Door unlocked " + isOpen);
            setOpen(isOpen);
        }

        public void setActive(boolean active) {
            this.isActive = active;
            this.blocker.getBody().setActive(active);
            this.sensor.getBody().setActive(active && this.isOpen);
        }

        public void setOpen(boolean open) {
            this.isOpen = open;
            if (this.isLocked) {
                return;
            }
            this.sensor.getBody().setActive(this.isActive && open);
            Filter filter = this.blocker.getFixture().getFilterData();
            filter.maskBits = open ? Globals.ProjectileCategory | Globals.ItemCategory : Globals.WallMask;
            this.blocker.getFixture().setFilterData(filter);
        }

        private void forceOpen(boolean open) {
            this.sensor.getBody().setActive(this.isActive && open);
            Filter filter = this.blocker.getFixture().getFilterData();
            filter.maskBits = open ? Globals.ProjectileCategory | Globals.ItemCategory : Globals.WallMask;
            this.blocker.getFixture().setFilterData(filter);
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

    private boolean isLoaded = false;
    private Stack<Consumer<RoomDataHandler>> callbacks = new Stack<>();

    private PhysicsScreen screen;
    private Consumer<Integer> onRoomChange;

    private Stack<ColliderObject> wallPool = new Stack<>();
    private ColliderObject blockerHolder;
    private SensorObject sensorHolder;
    private Generation generation;

    public RoomDataHandler(PhysicsScreen screen, Consumer<Integer> onRoomChange, Generation generation) {
        this.screen = screen;
        this.onRoomChange = onRoomChange;
        this.generation = generation;

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
        this.isLoaded = false;
    }

    public void loadRoomData(RoomData data, DungeonRoom room) {
        loadRoomData(data, room, false, true);
    }

    public void loadRoomData(RoomData data, DungeonRoom room, boolean isActive, boolean isOpen,
            RoomDataHandler[] neighbours) {
        final int myGeneration = this.generation.get();
        loadRoomData(data, room, isActive, isOpen);
        screen.schedule_generation(() -> this.setNeighbours(neighbours, myGeneration), generation, myGeneration);
    }

    private void loadRoomData(RoomData data, DungeonRoom room, boolean isActive, boolean isOpen) {
        this.map = data.map;
        loadWalls(data);
        loadDoors(data, room);

        final int myGeneration = this.generation.get();
        screen.schedule_generation(() -> {
            this.forceActive(isActive);
            this.forceOpen(isOpen);
            this.isLoaded = true;
            while (!callbacks.isEmpty()) {
                callbacks.pop().accept(this);
            }
        }, this.generation, myGeneration);
    }

    public void setNeighbours(RoomDataHandler[] neighbours, int generation) {
        int count = 0;
        for (RoomDataHandler neighbour : neighbours) {
            if (neighbour != null) {
                if (this.doors[count] != null) {
                    final int doorIndex = count; // Final variable for lambda
                    neighbour.addCallback(newData -> {
                        // System.out.println("Unlocking door " + doorIndex + " with generation " +
                        // generation);
                        if (this.generation.get() != generation) {
                            return; // Ignore callbacks for older generations
                        }
                        this.doors[doorIndex].unlock();
                    }, generation);
                }
                count++;
            }
        }
    }

    public void addCallback(Consumer<RoomDataHandler> callback, int generation) {
        if (this.generation.get() != generation) {
            return; // Ignore callbacks for older generations
        }
        Consumer<RoomDataHandler> myCallback = roomData -> {
            if (this.generation.get() != generation) {
                return; // Ignore callbacks for older generations
            }
            callback.accept(roomData);
        };
        System.out.println("Unlocking room data with generation " + generation);
        if (this.isLoaded) {
            myCallback.accept(this);
        } else {
            this.callbacks.push(myCallback);
        }
    }

    private void loadWalls(RoomData data) {
        this.walls = new ColliderObject[data.walls.length];
        int index = 0;
        final int myGeneration = this.generation.get();
        for (ColliderData wallData : data.walls) {
            final int wallIndex = index; // Final variable for lambda
            screen.schedule_generation(() -> loadWall(wallData, wallIndex), this.generation, myGeneration);
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
        final int myGeneration = this.generation.get();
        for (DoorData doorData : data.doors) {
            loadDoor(doorData, room, myGeneration);
        }
        screen.schedule_generation(() -> extendWalls(), this.generation, myGeneration);
    }

    private void loadDoor(DoorData data, DungeonRoom room, int myGeneration) {
        int direction = doorTypeToDirection(data.type);
        if (room.neighbours[direction] == null) {
            screen.schedule_generation(() -> {
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(data.collider.width, data.collider.height);
                wallPool.add(new ColliderObject(screen, 0, 0, 0, data.collider.x, data.collider.y, Globals.WallCategory,
                        Globals.WallMask, shape, BodyType.StaticBody, false));
            }, this.generation, myGeneration);
            return;
        }
        screen.schedule_generation(() -> {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(data.collider.width, data.collider.height);
            this.blockerHolder = new ColliderObject(screen, 0, 0, 0,
                    data.collider.x, data.collider.y, Globals.WallMask, Globals.WallCategory,
                    shape, BodyType.StaticBody, false);
        }, this.generation, myGeneration);

        screen.schedule_generation(() -> {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(data.sensor.width, data.sensor.height);
            this.sensorHolder = new SensorObject(screen, 0, 0, 0,
                    data.sensor.x, data.sensor.y, Globals.PlayerSensorCategory, Globals.PlayerCategory,
                    shape, data.type, false, fixture -> notifyRoomChange(data.type));
        }, this.generation, myGeneration);
        screen.schedule_generation(() -> {
            if (this.generation.get() != myGeneration) {
                return; // Ignore doors for older generations
            }
            this.doors[direction] = new DungeonDoor(this.blockerHolder, this.sensorHolder, data.spawnPoint);
        }, this.generation, myGeneration);
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