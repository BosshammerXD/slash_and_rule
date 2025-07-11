package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.EnemyComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class EnemySystem extends EntitySystem {
    private World world;
    private ImmutableArray<Entity> enemies;
    private ImmutableArray<Entity> players;

    private static class RayCast implements RayCastCallback {
        public boolean canSee = false;

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if (fixture.getFilterData().categoryBits == Globals.PlayerCategory) {
                canSee = true;
                return 0;
            }
            if (fixture.getFilterData().categoryBits != Globals.WallCategory) {
                return 1;
            }
            return 0;
        }
    }

    RayCast callback = new RayCast();

    public EnemySystem(World world, int priority) {
        super(priority);
        this.world = world;
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.enemies = engine.getEntitiesFor(Family
                .all(MovementComponent.class, TransformComponent.class, EnemyComponent.class, HealthComponent.class)
                .exclude(InactiveComponent.class).get());
        this.players = engine.getEntitiesFor(
                Family.all(TransformComponent.class, PlayerComponent.class).exclude(InactiveComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity enemy : enemies) {
            EnemyComponent enemyComp = Mappers.enemyMapper.get(enemy);
            PhysicsComponent physComp = Mappers.physicsMapper.get(enemy);

            moveTo(enemy, getVecToClosestPlayer(enemy));

            HealthComponent health = Mappers.healthMapper.get(enemy);
            if (health.health <= 0) {
                getEngine().removeEntity(enemy);
            }

            if (enemyComp.startPos != null) {
                physComp.body.setTransform(enemyComp.startPos, 0);
                enemyComp.startPos = null;
            }
        }
    }

    private void moveTo(Entity enemy, Vector2 direction) {
        if (enemy == null) {
            return;
        }
        EnemyComponent enemyComponent = Mappers.enemyMapper.get(enemy);
        MovementComponent movement = Mappers.movementMapper.get(enemy);

        Mappers.physicsMapper.get(enemy).body.setAwake(true);
        float distance = direction.len();

        if (distance == 0) {
            movement.velocity.setZero();
            enemyComponent.state = EnemyComponent.EnemyState.IDLE;
        } else if (Math.abs(distance - enemyComponent.attackRange) < 0.1) {
            enemyComponent.state = EnemyComponent.EnemyState.ATTACKING;
            movement.velocity.setZero();
        } else if (distance < enemyComponent.attackRange) {
            movement.velocity.set(direction.nor().scl(-movement.max_speed));
            enemyComponent.state = EnemyComponent.EnemyState.ATTACKING;
        } else {
            movement.velocity.set(direction.nor().scl(movement.max_speed));
            enemyComponent.state = EnemyComponent.EnemyState.CHASING;
        }
    }

    private Vector2 getVecToClosestPlayer(Entity enemy) {
        TransformComponent enemyTransform = Mappers.transformMapper.get(enemy);
        Vector2 closestPlayerPos = getClosestPlayerPos(enemyTransform);

        world.rayCast(callback, enemyTransform.position, closestPlayerPos);

        if (!callback.canSee) {
            return new Vector2(0, 0);
        }
        return closestPlayerPos.cpy().sub(enemyTransform.position);
    }

    private Vector2 getClosestPlayerPos(TransformComponent enemyTransform) {
        float closestDistance = Float.MAX_VALUE;
        Vector2 closestPos = new Vector2(0, 0);

        for (Entity player : players) {
            TransformComponent playerTransform = Mappers.transformMapper.get(player);
            float distance = enemyTransform.position.dst(playerTransform.position);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPos.set(playerTransform.position);
            }
        }

        return closestPos;
    }
}
