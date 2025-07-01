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
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.SpawnerComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.ColliderData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.DoorData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.UtilData;
import io.github.slash_and_rule.Utils.PhysicsBuilder;
import io.github.slash_and_rule.Interfaces.CollisionHandler;

public class DungeonBuilder {
    private EntityManager entityManager = new EntityManager();

    private Entity entity;

    private DungeonComponent dungeonComponent;
    private PhysicsComponent physicsComponent;
    private SensorComponent sensorComponent;
    private ArrayDeque<Fixture> physicsFixtures;
    private Fixture[][] doorFixtures;
    private Vector2[] spawnPoints = new Vector2[4]; // 0: left, 1: down, 2: right, 3: up

    private PhysicsBuilder physicsBuilder;

    public DungeonBuilder(PhysicsBuilder physicsBuilder) {
        this.physicsBuilder = physicsBuilder;
    }

    public Entity makeRoom(RoomData data, Object[] neighbours, CollisionHandler collisionHandler) {
        setup(data, collisionHandler);

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            makeWall(wall, physicsComponent.body);
        }

        for (DoorData door : data.doors) {
            makeDoor(door, neighbours, physicsComponent.body);
        }

        this.physicsComponent.fixtures = makeFixtures();
        this.dungeonComponent.spawnPoints = spawnPoints;

        this.dungeonComponent.doors = doorFixtures;

        entityManager.build(
                this.physicsComponent,
                this.dungeonComponent,
                this.sensorComponent);

        for (UtilData util : data.utils) {
            if (util.type.equals("entry")) {
                makeEntry(util);
            } else if (util.type.equals("spawner")) {
                makeSpawner(util);
            } else if (util.type.equals("chest")) {
                makeTreasure(util);
            }
        }

        this.entityManager.finish();

        return this.entity;
    }

    public void scheduledMakeRoom(ArrayDeque<Runnable> schedule, RoomData data, Object[] neighbours,
            CollisionHandler collisionHandler, Consumer<Entity> onFinish) {
        schedule.add(() -> setup(data, collisionHandler));

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            schedule.add(() -> makeWall(wall, physicsComponent.body));
        }

        for (DoorData door : data.doors) {
            schedule.add(() -> makeDoor(door, neighbours, physicsComponent.body));
        }

        schedule.add(() -> {
            this.physicsComponent.fixtures = makeFixtures();
            this.dungeonComponent.spawnPoints = spawnPoints;
            this.dungeonComponent.doors = doorFixtures;
        });

        schedule.add(() -> entityManager.build(
                this.physicsComponent,
                this.dungeonComponent,
                this.sensorComponent));

        schedule.add(() -> this.entityManager.finish());

        for (UtilData util : data.utils) {
            if (util.type.equals("entry")) {
                schedule.add(() -> makeEntry(util));
            } else if (util.type.equals("spawner")) {
                schedule.add(() -> makeSpawner(util));
            } else if (util.type.equals("chest")) {
                schedule.add(() -> makeTreasure(util));
            }
        }

        schedule.add(() -> {
            if (onFinish != null) {
                onFinish.accept(this.entity);
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
        this.dungeonComponent.map = data.map;
        this.physicsComponent = new PhysicsComponent();

        this.physicsComponent = new PhysicsComponent();
        this.physicsComponent.body = physicsBuilder.makeBody(BodyType.StaticBody, 0, false);

        this.sensorComponent = new SensorComponent();
        this.sensorComponent.collisionHandler = collisionHandler;

        this.physicsComponent.body.setUserData(this.entity);
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

    private Fixture buildWall(ColliderData wall, Body body) {
        Shape shape = makeRectangleShape(wall);
        return physicsBuilder.addFixture(body, shape, Globals.WallCategory, Globals.WallMask, false);
    }

    private void makeWall(ColliderData wall, Body body) {
        physicsFixtures.add(buildWall(wall, body));
    }

    private void makeDoor(DoorData door, Object[] neighbours, Body body) {
        // TODO: Add the picture of the door
        int index = dirToIndex(door.type);
        Object neighbour = neighbours[index];
        if (neighbour == null) {
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

    private void makeEntry(UtilData entry) {
        // TODO
        CircleShape shape = new CircleShape();
        shape.setPosition(new Vector2(entry.x, entry.y));
        shape.setRadius(entry.width);
        float x = entry.x + entry.width / 2f;
        float y = entry.y + entry.height / 2f;

        PhysicsComponent pC = new PhysicsComponent();
        pC.body = physicsBuilder.makeBody(x, y, BodyType.StaticBody, 0f, false);
        pC.fixtures.put(
                "Sensor",
                physicsBuilder.addFixture(pC.body, shape, Globals.SensorCategory, Globals.PlayerCategory, true));

        EntityManager.makeEntity(pC);
    }

    private void makeSpawner(UtilData spawner) {
        // TODO
        EntityManager.makeEntity(
                new SpawnerComponent());
    }

    private void makeTreasure(UtilData treasure) {
        // TODO
        CircleShape shape = new CircleShape();
        shape.setPosition(new Vector2(treasure.x, treasure.y));
        shape.setRadius(treasure.width);
        float x = treasure.x + treasure.width / 2f;
        float y = treasure.y + treasure.height / 2f;

        PhysicsComponent pC = new PhysicsComponent();
        pC.body = physicsBuilder.makeBody(x, y, BodyType.StaticBody, 0f, false);
        pC.fixtures.put(
                "Sensor",
                physicsBuilder.addFixture(pC.body, shape, Globals.SensorCategory, Globals.PlayerCategory, true));

        EntityManager.makeEntity(pC);
    }

}
