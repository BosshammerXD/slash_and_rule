package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.ArrayDeque;
import java.util.Arrays;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Bases.BaseEnemy;

public class EnemyPicker {
    private BaseEnemy[] enemies;

    public EnemyPicker() {
    }

    public void setEnemies(BaseEnemy[] enemies) {
        this.enemies = enemies;
        Arrays.sort(this.enemies, (a, b) -> Integer.compare(a.getCost(), b.getCost()));
    }

    public ArrayDeque<BaseEnemy> pickEnemies(int balance) {
        if (enemies == null || enemies.length == 0) {
            return new ArrayDeque<>();
        }

        int maxIndex = enemies.length;
        ArrayDeque<BaseEnemy> pickedEnemies = new ArrayDeque<>();

        while (maxIndex > 0) {
            int index = Globals.random.nextInt(maxIndex);
            BaseEnemy enemy = enemies[index];
            if (enemy.getCost() <= balance) {
                pickedEnemies.add(enemy);
                balance -= enemy.getCost();
            } else {
                maxIndex = index;
            }
        }

        return pickedEnemies;
    }
}
