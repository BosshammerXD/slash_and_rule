package io.github.slash_and_rule.Ashley.Components.DungeonComponents.Enemies;

import com.badlogic.gdx.math.Vector2;

public class JumperComponent extends EnemyAtkComponent {
    public Vector2 jumpStart = new Vector2();
    public float vulnerableTime = 0.5f; // Time the enemy is vulnerable after jumping
    public float jumpTime = 0.1f;
    public float time = 0f;
}
