package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Components.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.ColliderData;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

public class DungeonBuilder {
    private EntityManager entityManager = new EntityManager();
    private RoomData roomData;

    private Entity entity;

    private DungeonComponent dungeonComponent;
    private PhysicsComponent physicsComponent;
    private Fixture[] physicsFixtures;

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

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            makeWall(i, wall.x, wall.y, wall.width, wall.height, physicsComponent.body);
        }
    }

    private void makeWall(int index, float x, float y, float width, float height, Body body) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2, new Vector2(x, y), 0);

        physicsFixtures[index] = physicsBuilder.addFixture(body, shape, Globals.WallCategory, Globals.WallMask);
    }

}
