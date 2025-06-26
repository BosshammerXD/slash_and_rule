package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Components.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.ColliderData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.DoorData;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

public class DungeonBuilder {
    private EntityManager entityManager = new EntityManager();
    private RoomData roomData;

    private Entity entity;

    private DungeonComponent dungeonComponent;
    private PhysicsComponent physicsComponent;
    private Fixture[] physicsFixtures;
    private Fixture[] doorFixtures;

    private PhysicsBuilder physicsBuilder;

    public DungeonBuilder(RoomData data, PhysicsBuilder physicsBuilder) {
        this.physicsBuilder = physicsBuilder;

        entity = entityManager.reset();

        this.roomData = data;
        this.dungeonComponent = new DungeonComponent();
        this.dungeonComponent.map = data.map;

        this.physicsComponent = new PhysicsComponent();
        this.physicsComponent.body = physicsBuilder.makeBody(
                entity, BodyType.StaticBody, 0, false);

        this.physicsFixtures = new Fixture[data.walls.length];
        this.doorFixtures = new Fixture[data.doors.length * 2];

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            makeWall(i, wall, physicsComponent.body);
        }

        for (int i = 0; i < data.doors.length; i++) {
            DoorData door = data.doors[i];
            makeDoor(i * 2, door, physicsComponent.body);
        }
    }

    private Shape makeRectangleShape(ColliderData collider) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(collider.width / 2, collider.height / 2, new Vector2(collider.x, collider.y), 0);
        return shape;
    }

    private Fixture buildWall(ColliderData wall, Body body) {
        Shape shape = makeRectangleShape(wall);
        return physicsBuilder.addFixture(body, shape, Globals.WallCategory, Globals.WallMask, false);
    }

    private void makeWall(int index, ColliderData wall, Body body) {
        physicsFixtures[index] = buildWall(wall, body);
    }

    private void makeDoor(int index, DoorData door, Body body) {
        ColliderData doorwall = door.collider;
        doorFixtures[index] = buildWall(doorwall, body);

        Shape doorShape = makeRectangleShape(door.sensor);
        doorFixtures[index + 1] = physicsBuilder.addFixture(
                body, doorShape, Globals.PlayerSensorCategory, (short) 0, true);

    }

}
