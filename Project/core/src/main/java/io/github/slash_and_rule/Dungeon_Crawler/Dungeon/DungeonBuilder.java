package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.ArrayDeque;
import java.util.function.Consumer;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Builder.PhysCompBuilder;
import io.github.slash_and_rule.Ashley.Builder.RenderBuilder;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.BackgroundComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.ColliderData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.DoorData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.UtilData;
import io.github.slash_and_rule.Interfaces.CollisionHandler;
import io.github.slash_and_rule.Utils.ShapeBuilder;

public class DungeonBuilder {
    private PhysCompBuilder physicsCompBuilder;
    private RenderBuilder renderBuilder = new RenderBuilder();

    public DungeonBuilder(PhysCompBuilder physCompBuilder) {
        this.physicsCompBuilder = physCompBuilder;
    }

    public RoomEntity makeRoom(RoomData data, Object[] neighbours, CollisionHandler collisionHandler) {
        ArrayDeque<Entity> utilEntities = new ArrayDeque<>();
        ArrayDeque<Vector2> spawnerPositions = new ArrayDeque<>();

        Entity entity = new Entity();
        DungeonComponent dungeonComponent = new DungeonComponent();
        PhysicsComponent physicsComp = physicsCompBuilder.make(BodyType.StaticBody, 0f, false);
        int[] index = { 0 };
        Fixture[][] doorFixtures = new Fixture[4][];
        Vector2[] spawnPoints = new Vector2[4];

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            buildWall(wall, physicsComp, index);
        }

        renderBuilder.begin();
        for (DoorData door : data.doors) {
            makeDoor(door, neighbours, physicsComp, index, doorFixtures, spawnPoints);
        }
        renderBuilder.end(entity);

        dungeonComponent.doors = doorFixtures;

        EntityManager.build(entity,
                physicsComp,
                dungeonComponent,
                new SensorComponent(collisionHandler),
                new TransformComponent(),
                new BackgroundComponent());

        for (UtilData util : data.utils) {
            if (util.type.equals("entry")) {
                utilEntities.add(makeEntry(util));
            } else if (util.type.equals("spawner")) {
                spawnerPositions.add(new Vector2(util.x, util.y));
            } else if (util.type.equals("chest")) {
                utilEntities.add(makeTreasure(util));
            }
        }

        return new RoomEntity(entity, getSpawnPoints(spawnPoints), data.map, utilEntities.toArray(new Entity[0]),
                spawnerPositions.toArray(new Vector2[0]));
    }

    public void scheduledMakeRoom(ArrayDeque<Runnable> schedule, RoomData data, Object[] neighbours,
            CollisionHandler collisionHandler, Consumer<RoomEntity> onFinish) {
        ArrayDeque<Entity> utilEntities = new ArrayDeque<>();
        ArrayDeque<Vector2> spawnerPositions = new ArrayDeque<>();

        final Entity entity = new Entity();
        final DungeonComponent dungeonComponent = new DungeonComponent();
        final PhysicsComponent physicsComponent = physicsCompBuilder.make(BodyType.StaticBody, 0f,
                false);

        final int[] index = { 0 }; // Used to keep track of wall indices
        final Fixture[][] doorFixtures = new Fixture[4][];
        final Vector2[] spawnPoints = new Vector2[4]; // 0: left, 1: down, 2: right, 3: up

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            schedule.add(() -> buildWall(wall, physicsComponent, index));
        }

        schedule.add(() -> {
            renderBuilder.begin();
            for (DoorData door : data.doors) {
                makeDoor(door, neighbours, physicsComponent, index, doorFixtures, spawnPoints);
            }
            renderBuilder.end(entity);
        });

        schedule.add(() -> {
            dungeonComponent.doors = doorFixtures;
        });

        schedule.add(() -> EntityManager.build(entity,
                dungeonComponent,
                physicsComponent,
                new SensorComponent(collisionHandler),
                new TransformComponent(),
                new BackgroundComponent()));

        for (UtilData util : data.utils) {
            if (util.type.equals("entry")) {
                schedule.add(() -> utilEntities.add(makeEntry(util)));
            } else if (util.type.equals("spawner")) {
                schedule.add(() -> spawnerPositions.add(new Vector2(util.x, util.y)));
            } else if (util.type.equals("chest")) {
                schedule.add(() -> utilEntities.add(makeTreasure(util)));
            }
        }

        schedule.add(() -> {
            if (onFinish != null) {
                System.out.println("Final room body position: " + physicsComponent.body.getPosition());
                onFinish.accept(new RoomEntity(entity, getSpawnPoints(spawnPoints), data.map,
                        utilEntities.toArray(new Entity[0]),
                        spawnerPositions.toArray(new Vector2[0])));
            }
        });
    }

    private int dirToIndex(String direction) {
        switch (direction.toLowerCase()) {
            case "left":
                return 0;
            case "bottom":
                return 1;
            case "right":
                return 2;
            case "top":
                return 3;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    private Fixture buildWall(ColliderData wall, PhysicsComponent comp, int[] index) {
        Shape shape = ShapeBuilder.rect(wall.x, wall.y, wall.width * 2f, wall.height * 2f);
        return physicsCompBuilder.asyncAdd(comp, "wall_" + index[0]++, shape, Globals.WallCategory, Globals.WallMask,
                false);
    }

    private void addWallSprite(ColliderData wall, int direction) {
        String spriteName;
        float width = wall.width * 2;
        float height = wall.height * 2;
        float x = wall.x - wall.width;
        float y = wall.y - wall.height;
        switch (direction) {
            case 0:
                spriteName = "Dungeon_Wall_Left";
                break;
            case 1:
                spriteName = "Dungeon_Wall_Bottom";
                break;
            case 2:
                spriteName = "Dungeon_Wall_Right";
                break;
            case 3:
                spriteName = "Dungeon_Wall_Top";
                height *= 2;
                break;
            default:
                return;
        }
        renderBuilder.add(null, spriteName, 0, width, height, x, y);
    }

    private void makeDoor(DoorData door, Object[] neighbours, PhysicsComponent comp, int[] i, Fixture[][] doorFixtures,
            Vector2[] spawnPoints) {
        // TODO: Add the picture of the door
        int index = dirToIndex(door.type);
        Object neighbour = neighbours[index];
        if (neighbour == null) {
            addWallSprite(door.collider, index);
            buildWall(door.collider, comp, i);
            return;
        }

        doorFixtures[index] = new Fixture[2];

        ColliderData doorWall = door.collider;
        doorFixtures[index][0] = buildWall(doorWall, comp, i);

        ColliderData doorSensor = door.sensor;
        Shape doorShape = ShapeBuilder.rect(doorSensor.x, doorSensor.y, doorSensor.width * 2f, doorSensor.height * 2f);
        doorFixtures[index][1] = physicsCompBuilder.asyncAdd(comp, "door_" + index, doorShape, Globals.SensorCategory,
                (short) 0, true);

        // Set the door direction as UserData so the collision handler knows which door
        // was touched
        doorFixtures[index][1].setUserData(index);

        spawnPoints[index] = new Vector2(door.spawnPoint[0], door.spawnPoint[1]);
    }

    private Entity makeEntry(UtilData entry) {
        // TODO
        Shape shape = ShapeBuilder.circ(entry.x + entry.width, entry.y + entry.height, entry.width);

        physicsCompBuilder.begin(BodyType.StaticBody, 0f, false);
        physicsCompBuilder.add("Sensor", shape, Globals.SensorCategory, Globals.PlayerCategory, true);

        Entity entity = new Entity();
        EntityManager.build(entity, physicsCompBuilder.end());

        return entity;
    }

    private Entity makeTreasure(UtilData treasure) {
        // TODO
        Shape shape = ShapeBuilder.circ(treasure.x + treasure.width, treasure.y + treasure.height, treasure.width);

        physicsCompBuilder.begin(BodyType.StaticBody, 0f, false);
        physicsCompBuilder.add("Sensor", shape, Globals.SensorCategory, Globals.PlayerCategory, true);

        Entity entity = new Entity();
        EntityManager.build(entity, physicsCompBuilder.end());

        return entity;
    }

    private Vector2[] getSpawnPoints(Vector2[] spawnPoints) {
        Vector2[] points = new Vector2[4];
        for (int i = 0; i < spawnPoints.length; i++) {
            if (spawnPoints[i] != null) {
                points[i] = spawnPoints[i].cpy();
            }
        }
        return points;
    }
}
