package io.github.slash_and_rule.Ashley.Systems.EnemySystems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;

import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.Enemies.EnemyComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.Enemies.JumperComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.WeaponStates;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class JumperSystem extends IteratingSystem {
    public JumperSystem(int priority) {
        super(Family.all(JumperComponent.class, WeaponComponent.class, PhysicsComponent.class, EnemyComponent.class)
                .exclude(InactiveComponent.class).get(), priority);
    }

    @Override
    public void addedToEngine(Engine engine) {
        // TODO Auto-generated method stub
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                JumperComponent jumper = Mappers.jumperMapper.get(entity);
                PhysicsComponent physics = Mappers.physicsMapper.get(entity);
                WeaponComponent weapon = Mappers.weaponMapper.get(entity);
                jumper.time = 0f;

                jumper.jumpStart.set(physics.body.getPosition());

                weapon.state = WeaponStates.ATTACKING;

                physics.body.setActive(false);
            }

            @Override
            public void entityRemoved(Entity entity) {
                EnemyComponent enemy = Mappers.enemyMapper.get(entity);

                enemy.state = EnemyComponent.EnemyState.IDLE;
            }
        });
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        JumperComponent jumper = Mappers.jumperMapper.get(entity);
        PhysicsComponent physics = Mappers.physicsMapper.get(entity);
        WeaponComponent weapon = Mappers.weaponMapper.get(entity);

        jumper.time += deltaTime;

        if (jumper.time < jumper.jumpTime) {
            System.out.println("JumperSystem: " + jumper.time + " / " + jumper.jumpTime);
            System.out.println("Target Position: " + jumper.targetPosition);
            Vector2 newPosition = jumper.jumpStart.cpy()
                    .add(jumper.targetPosition.cpy().scl(jumper.time / jumper.jumpTime));
            physics.body.setTransform(newPosition, physics.body.getAngle());
        }

        if (jumper.time >= jumper.jumpTime) {
            physics.body.setActive(true);
        }

        if (weapon.state == WeaponStates.IDLE) {
            jumper.targetPosition.setZero();
            entity.remove(JumperComponent.class);
        }
    }

}
