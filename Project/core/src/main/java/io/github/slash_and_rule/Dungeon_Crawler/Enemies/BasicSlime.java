package io.github.slash_and_rule.Dungeon_Crawler.Enemies;

import java.util.HashMap;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Bases.BaseEnemy;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

public class BasicSlime extends BaseEnemy {
    public BasicSlime(PhysicsBuilder physicsBuilder, EntityManager entityManager) {
        super(physicsBuilder, entityManager);
    }

    @Override
    protected EnemyData makeEnemyData(EnemyData data) {
        data.health = 50;
        data.max_speed = 2f;
        data.attackRange = 1f;

        return data;
    }

    @Override
    protected void addTextures(RenderableComponent renderableComponent) {

    }

    @Override
    protected void addFixtures(PhysicsComponent fixtures) {
        Body body = fixtures.body;
        HashMap<String, Fixture> fixtureMap = fixtures.fixtures;

        CircleShape colliderShape = new CircleShape();
        colliderShape.setRadius(0.5f);

        fixtureMap.put("Collider",
                physicsBuilder.addFixture(body, colliderShape, Globals.EnemyCategory, Globals.ColEnemyMask, false));

        CircleShape hurtBoxShape = new CircleShape();
        hurtBoxShape.setRadius(0.5f);

        fixtureMap.put("HurtBox",
                physicsBuilder.addFixture(body, hurtBoxShape, Globals.EnemyCategory, Globals.HitboxCategory, true));
    }

    @Override
    public int getCost() {
        return 10;
    }
}
