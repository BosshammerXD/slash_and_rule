package io.github.slash_and_rule.Dungeon_Crawler.Enemies;

import java.util.HashMap;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Builder.WeaponBuilder;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Bases.BaseEnemy;
import io.github.slash_and_rule.Utils.PhysicsBuilder;
import io.github.slash_and_rule.Utils.ShapeBuilder;

public class BasicSlime extends BaseEnemy {
    public BasicSlime(PhysicsBuilder physicsBuilder, WeaponBuilder weaponBuilder, EntityManager entityManager) {
        super(physicsBuilder, weaponBuilder, entityManager);
    }

    @Override
    protected EnemyData makeEnemyData(EnemyData data) {
        data.health = 30;
        data.max_speed = 2f;

        data.attackRange = 2f;

        return data;
    }

    @Override
    protected WeaponComponent makeWeapon() {
        weaponBuilder.begin(5, 10f, 1f, Globals.PlayerCategory);

        Shape hitbox = ShapeBuilder.circ(0.5f);

        weaponBuilder.addHitbox(0.1f, 0.2f, hitbox);

        return weaponBuilder.end();
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
