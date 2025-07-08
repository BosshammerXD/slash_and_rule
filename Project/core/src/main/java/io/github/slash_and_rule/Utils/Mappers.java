package io.github.slash_and_rule.Utils;

import com.badlogic.ashley.core.ComponentMapper;

import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.EnemyComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;

public class Mappers {
        public static ComponentMapper<RenderableComponent> renderableMapper = ComponentMapper
                        .getFor(RenderableComponent.class);
        public static ComponentMapper<PhysicsComponent> physicsMapper = ComponentMapper
                        .getFor(PhysicsComponent.class);
        public static ComponentMapper<SensorComponent> sensorMapper = ComponentMapper
                        .getFor(SensorComponent.class);
        public static ComponentMapper<ControllableComponent> controllableMapper = ComponentMapper
                        .getFor(ControllableComponent.class);
        public static ComponentMapper<MovementComponent> movementMapper = ComponentMapper
                        .getFor(MovementComponent.class);
        public static ComponentMapper<TransformComponent> transformMapper = ComponentMapper
                        .getFor(TransformComponent.class);
        public static ComponentMapper<DungeonComponent> dungeonMapper = ComponentMapper
                        .getFor(DungeonComponent.class);
        public static ComponentMapper<WeaponComponent> weaponMapper = ComponentMapper
                        .getFor(WeaponComponent.class);
        public static ComponentMapper<HealthComponent> healthMapper = ComponentMapper
                        .getFor(HealthComponent.class);
        public static ComponentMapper<EnemyComponent> enemyMapper = ComponentMapper
                        .getFor(EnemyComponent.class);
}
