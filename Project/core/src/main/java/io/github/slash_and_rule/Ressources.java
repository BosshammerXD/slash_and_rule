package io.github.slash_and_rule;

import java.util.HashMap;

public class Ressources {
    public static final String[] ALL_RESSOURCES = {
            Ressources.COIN, Ressources.SLIME_GEM
    };

    public static final String COIN = "Coin";
    public static final String SLIME_GEM = "SlimeGem";

    public static class ItemData {
        public String name;
        public int amount;

        public ItemData(String name, int amount) {
            this.name = name;
            this.amount = amount;
        }

        public ItemData(String name) {
            this(name, 0);
        }

        public void add(int amount) {
            this.amount += amount;
        }
    }

    private static final HashMap<String, ItemData> collectedItems = new HashMap<>();

    public static final HashMap<String, ItemData> items = new HashMap<>();

    public static final void addItem(String itemName, int amount) {
        collectedItems.computeIfAbsent(itemName, k -> new ItemData(itemName)).add(amount);
    }

    public static final void DungeonLeft() {
        collectedItems.forEach((name, itemData) -> {
            items.computeIfAbsent(name, k -> new ItemData(name)).add(itemData.amount);
        });
        collectedItems.clear();
    }

    public static final void playerDied() {
        collectedItems.clear();
    }
}
