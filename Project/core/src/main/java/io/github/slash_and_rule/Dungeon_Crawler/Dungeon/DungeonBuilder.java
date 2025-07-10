package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.function.Consumer;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Builder.RenderBuilder;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.BackgroundComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.ColliderData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.DoorData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.UtilData;
import io.github.slash_and_rule.Utils.PhysicsBuilder;
import io.github.slash_and_rule.Interfaces.CollisionHandler;

public class DungeonBuilder {

    private EntityManager entityManager;

    private Entity entity;

    private DungeonComponent dungeonComponent;
    private PhysicsComponent physicsComponent;
    private SensorComponent sensorComponent;
    private RenderableComponent renderableComponent;
    private ArrayDeque<Fixture> physicsFixtures;
    private Fixture[][] doorFixtures;
    private Vector2[] spawnPoints = new Vector2[4]; // 0: left, 1: down, 2: right, 3: up

    private PhysicsBuilder physicsBuilder;
    private RenderBuilder renderBuilder = new RenderBuilder();

    public DungeonBuilder(PhysicsBuilder physicsBuilder, EntityManager entityManager) {
        this.physicsBuilder = physicsBuilder;
        this.entityManager = entityManager;
    }

    public RoomEntity makeRoom(RoomData data, Object[] neighbours, CollisionHandler collisionHandler) {
        setup(data, collisionHandler);

        ArrayDeque<Entity> utilEntities = new ArrayDeque<>();
        ArrayDeque<Vector2> spawnerPositions = new ArrayDeque<>();

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            makeWall(wall, physicsComponent.body);
        }

        for (DoorData door : data.doors) {
            makeDoor(door, neighbours, physicsComponent.body);
        }

        this.physicsComponent.fixtures = makeFixtures();

        this.dungeonComponent.doors = doorFixtures;

        entityManager.build(
                this.physicsComponent,
                this.dungeonComponent,
                this.sensorComponent,
                this.renderableComponent,
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

        return new RoomEntity(entity, getSpawnPoints(), data.map, utilEntities.toArray(new Entity[0]),
                spawnerPositions.toArray(new Vector2[0]));
    }

    public void scheduledMakeRoom(ArrayDeque<Runnable> schedule, RoomData data, Object[] neighbours,
            CollisionHandler collisionHandler, Consumer<RoomEntity> onFinish) {
        schedule.add(() -> setup(data, collisionHandler));
        ArrayDeque<Entity> utilEntities = new ArrayDeque<>();
        ArrayDeque<Vector2> spawnerPositions = new ArrayDeque<>();

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            schedule.add(() -> makeWall(wall, physicsComponent.body));
        }

        for (DoorData door : data.doors) {
            schedule.add(() -> makeDoor(door, neighbours, physicsComponent.body));
        }

        schedule.add(() -> {
            this.physicsComponent.fixtures = makeFixtures();
            this.dungeonComponent.doors = doorFixtures;
        });

        schedule.add(() -> entityManager.build(
                this.physicsComponent,
                this.dungeonComponent,
                this.sensorComponent,
                this.renderableComponent,
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
                onFinish.accept(new RoomEntity(entity, getSpawnPoints(), data.map, utilEntities.toArray(new Entity[0]),
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

    private void setup(RoomData data, CollisionHandler collisionHandler) {
        this.entity = entityManager.reset();

        this.physicsFixtures = new ArrayDeque<>();
        this.doorFixtures = new Fixture[4][];

        this.dungeonComponent = new DungeonComponent();
        this.physicsComponent = new PhysicsComponent();

        this.physicsComponent = new PhysicsComponent();
        this.physicsComponent.body = physicsBuilder.makeBody(BodyType.StaticBody, 0, false);

        this.sensorComponent = new SensorComponent();
        this.sensorComponent.collisionHandler = collisionHandler;

        this.renderableComponent = new RenderableComponent();
    }

    private HashMap<String, Fixture> makeFixtures() {
        HashMap<String, Fixture> doorFixtures = new HashMap<>();
        int index = 0;
        for (Fixture fixture : physicsFixtures) {
            String name = "wall_" + index++;
            doorFixtures.put(name, fixture);
        }
        return doorFixtures;
    }

    private Shape makeRectangleShape(ColliderData collider) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(collider.width, collider.height, new Vector2(collider.x, collider.y), 0);
        return shape;
    }

    private Shape makeCircleShape(UtilData util) {
        CircleShape shape = new CircleShape();
        shape.setPosition(new Vector2(util.x + util.width, util.y + util.height));
        shape.setRadius(util.width);
        return shape;
    }

    private Fixture buildWall(ColliderData wall, Body body) {
        Shape shape = makeRectangleShape(wall);
        return physicsBuilder.addFixture(body, shape, Globals.WallCategory, Globals.WallMask, false);
    }

    private void makeWall(ColliderData wall, Body body) {
        physicsFixtures.add(buildWall(wall, body));
    }

    private void addWallSprite(ColliderData wall, int direction) {
        String spriteName;
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
                break;
            default:
                return;
        }
        renderBuilder.begin();
        // TODO
        renderBuilder.add(null , spriteName, direction, direction, direction, direction, direction);
        renderBuilder.end(entity);
        
    }

    private void makeDoor(DoorData door, Object[] neighbours, Body body) {
        // TODO: Add the picture of the door
        int index = dirToIndex(door.type);
        Object neighbour = neighbours[index];
        if (neighbour == null) {
            addWallSprite(door.collider, index);
            makeWall(door.collider, body);
            return;
        }

        doorFixtures[index] = new Fixture[2];

        ColliderData doorwall = door.collider;
        doorFixtures[index][0] = buildWall(doorwall, body);

        Shape doorShape = makeRectangleShape(door.sensor);
        doorFixtures[index][1] = physicsBuilder.addFixture(
                body, doorShape, Globals.SensorCategory, (short) 0, true);

        // Set the door direction as UserData so the collision handler knows which door
        // was touched
        doorFixtures[index][1].setUserData(index);

        spawnPoints[index] = new Vector2(door.spawnPoint[0], door.spawnPoint[1]);
    }

    private Entity makeEntry(UtilData entry) {
        // TODO
        Shape shape = makeCircleShape(entry);

        PhysicsComponent pC = new PhysicsComponent();
        pC.body = physicsBuilder.makeBody(BodyType.StaticBody, 0f, false);
        pC.fixtures.put(
                "Sensor",
                physicsBuilder.addFixture(pC.body, shape, Globals.SensorCategory, Globals.PlayerCategory, true));

        return entityManager.makeEntity(pC);
    }

    private Entity makeTreasure(UtilData treasure) {
        // TODO
        Shape shape = makeCircleShape(treasure);

        PhysicsComponent pC = new PhysicsComponent();
        pC.body = physicsBuilder.makeBody(BodyType.StaticBody, 0f, false);
        pC.fixtures.put(
                "Sensor",
                physicsBuilder.addFixture(pC.body, shape, Globals.SensorCategory, Globals.PlayerCategory, true));

        return entityManager.makeEntity(pC);
    }

    private Vector2[] getSpawnPoints() {
        Vector2[] points = new Vector2[4];
        for (int i = 0; i < spawnPoints.length; i++) {
            if (spawnPoints[i] != null) {
                points[i] = spawnPoints[i].cpy();
            }
        }
        return points;
    }
}
