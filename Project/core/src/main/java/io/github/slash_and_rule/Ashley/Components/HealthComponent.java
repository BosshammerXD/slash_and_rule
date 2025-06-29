package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;

public class HealthComponent implements Component {
    public int appliedDamage = 0;
    public int health;
    public int maxHealth;
    public float time = -1f;
    public float invulnerabilityTime = 0.1f; // Time during which the entity is invulnerable after taking damage

    public HealthComponent(int health, int maxHealth) {
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public HealthComponent() {
        this(100, 100); // Default values
    }
}
