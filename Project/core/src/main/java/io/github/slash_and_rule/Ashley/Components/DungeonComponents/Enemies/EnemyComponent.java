package io.github.slash_and_rule.Ashley.Components.DungeonComponents.Enemies;

import com.badlogic.ashley.core.Component;

public class EnemyComponent implements Component {
    public static enum EnemyState {
        IDLE, CHASING, ATTACKING, DEAD
    }

    public EnemyState state = EnemyState.IDLE;
    public float attackRange;
    public EnemyAtkComponent atkComponent;
}
