package io.github.slash_and_rule.Ashley.Components.DungeonComponents.Enemies;

import com.badlogic.ashley.core.Component;

public class EnemyComponent implements Component {
    public static class Drop {
        public String item;
        public float chance;

        public Drop(String item, float chance) {
            this.item = item;
            this.chance = chance;
        }
    }

    public static enum EnemyState {
        IDLE, CHASING, ATTACKING, DEAD
    }

    public EnemyState state = EnemyState.IDLE;
    public float attackRange;
    public EnemyAtkComponent atkComponent;

    public Drop[] drops;
}
