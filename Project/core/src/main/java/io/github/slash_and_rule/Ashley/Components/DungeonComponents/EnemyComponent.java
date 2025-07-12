package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import com.badlogic.ashley.core.Component;

public class EnemyComponent implements Component {
    public static enum EnemyType {
        JUMPER, CHASER, SHOOTER, MEELE
    }

    public static enum EnemyState {
        IDLE, CHASING, ATTACKING, DEAD
    }

    public EnemyState state = EnemyState.IDLE;
    public EnemyType type;
    public float attackRange;

    public EnemyComponent(float attackRange, EnemyType type) {
        this.attackRange = attackRange;
        this.type = type;
    }

    public EnemyComponent() {
        this(1f, EnemyType.MEELE);
    }
}
