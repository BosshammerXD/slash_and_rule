package io.github.slash_and_rule.Utils;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

import io.github.slash_and_rule.Ashley.Components.ChildComponent;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.InvulnerableComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.ParentComponent;
import io.github.slash_and_rule.Ashley.Components.StateComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.AnimatedComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DoorComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.EntryComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.Enemies.EnemyComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.Enemies.JumperComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;

public class Mappers {
        private static <T extends Component> ComponentMapper<T> get(Class<T> type) {
                return ComponentMapper.getFor(type);
        }

        public static ComponentMapper<PhysicsComponent> physicsMapper = get(PhysicsComponent.class);
        public static ComponentMapper<SensorComponent> sensorMapper = get(SensorComponent.class);
        public static ComponentMapper<ControllableComponent> controllableMapper = get(ControllableComponent.class);
        public static ComponentMapper<MovementComponent> movementMapper = get(MovementComponent.class);
        public static ComponentMapper<TransformComponent> transformMapper = get(TransformComponent.class);
        public static ComponentMapper<DungeonComponent> dungeonMapper = get(DungeonComponent.class);
        public static ComponentMapper<WeaponComponent> weaponMapper = get(WeaponComponent.class);
        public static ComponentMapper<HealthComponent> healthMapper = get(HealthComponent.class);
        public static ComponentMapper<InvulnerableComponent> invulnerableMapper = get(InvulnerableComponent.class);
        public static ComponentMapper<EnemyComponent> enemyMapper = get(EnemyComponent.class);
        public static ComponentMapper<AnimatedComponent> animatedMapper = get(AnimatedComponent.class);
        public static ComponentMapper<StateComponent> stateMapper = get(StateComponent.class);
        public static ComponentMapper<InactiveComponent> inactiveMapper = get(InactiveComponent.class);
        public static ComponentMapper<ParentComponent> parentMapper = get(ParentComponent.class);
        public static ComponentMapper<ChildComponent> childMapper = get(ChildComponent.class);
        public static ComponentMapper<DoorComponent> doorMapper = get(DoorComponent.class);
        public static ComponentMapper<EntryComponent> entryMapper = get(EntryComponent.class);
        public static ComponentMapper<JumperComponent> jumperMapper = get(JumperComponent.class);

        public static ComponentMapper<MidfieldComponent> midfieldMapper = get(MidfieldComponent.class);
}
