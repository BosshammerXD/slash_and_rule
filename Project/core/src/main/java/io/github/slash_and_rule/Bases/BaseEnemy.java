package io.github.slash_and_rule.Bases;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Builder.WeaponBuilder;
import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.EnemyComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.ProjectileData;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

public abstract class BaseEnemy {
    protected static class EnemyData {
        public int health = 100;
        public float max_speed = 5f;
        public float attackRange = 0.5f;

        public int damage = 10;
        public float weight = 1f;
        public float attackCooldown = 1f;

        public EnemyData() {
        }
    }

    protected PhysicsBuilder physicsBuilder;
    protected WeaponBuilder weaponBuilder;
    protected EntityManager entityManager;

    public BaseEnemy(PhysicsBuilder physicsBuilder, WeaponBuilder weaponBuilder, EntityManager entityManager) {
        this.physicsBuilder = physicsBuilder;
        this.weaponBuilder = weaponBuilder;
        this.entityManager = entityManager;
    }

    public final Entity makeEntity(Vector2 position) {
        EnemyData data = makeEnemyData(new EnemyData());
        Entity entity = entityManager.reset();
        Vector2 pos = position.cpy();

        TransformComponent transformComponent = new TransformComponent(pos, 0f);
        MovementComponent movementComponent = new MovementComponent();
        movementComponent.max_speed = data.max_speed;

        HealthComponent healthComponent = new HealthComponent(data.health);

        RenderableComponent renderableComponent = new RenderableComponent();
        addTextures(renderableComponent);

        PhysicsComponent physicsComponent = new PhysicsComponent();
        physicsComponent.body = physicsBuilder.makeBody(BodyType.DynamicBody, 6f, true);
        addFixtures(physicsComponent);

        WeaponComponent weaponComponent = makeWeapon();

        EnemyComponent enemyComponent = new EnemyComponent(data.attackRange);
        enemyComponent.startPos = position.cpy();

        entityManager.build(
                transformComponent,
                movementComponent,
                healthComponent,
                renderableComponent,
                physicsComponent,
                weaponComponent,
                enemyComponent);

        entityManager.finish();

        return entity;
    }

    protected abstract WeaponComponent makeWeapon();

    protected abstract void addTextures(RenderableComponent renderableComponent);

    protected abstract void addFixtures(PhysicsComponent physicsComponent);

    protected abstract EnemyData makeEnemyData(EnemyData data);

    public abstract int getCost();
}
