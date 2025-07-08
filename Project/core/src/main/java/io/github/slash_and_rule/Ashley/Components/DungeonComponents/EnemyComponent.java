package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class EnemyComponent implements Component {
    public enum EnemyState {
        IDLE, CHASING, ATTACKING, DEAD
    }

    public EnemyState state = EnemyState.IDLE;
    public float attackRange;
    public Vector2 startPos = null;

    public EnemyComponent(float attackRange) {
        this.attackRange = attackRange;
    }

    public EnemyComponent() {
        this(1f);
    }
}
