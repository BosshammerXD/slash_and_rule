package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;

public class HealthComponent implements Component {
    public int health;
    public int maxHealth;

    public HealthComponent(int health, int maxHealth) {
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public HealthComponent() {
        this(100, 100); // Default values
    }

    public void heal(int amount) {
        health = Math.min(health + amount, maxHealth);
    }

    public void damage(int amount) {
        health = Math.max(health - amount, 0);
    }
}
