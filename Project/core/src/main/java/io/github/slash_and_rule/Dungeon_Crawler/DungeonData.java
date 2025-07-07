package io.github.slash_and_rule.Dungeon_Crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Json;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Bases.BaseEnemy;
import io.github.slash_and_rule.Dungeon_Crawler.Enemies.BasicSlime;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.PhysicsBuilder;
import io.github.slash_and_rule.Utils.RandomCollection;

public class DungeonData {
    private static class JsonData {
        public String start;
        public HashMap<String, Double> filler;
        public HashMap<String, Double> leaf;
        public String end;
        public List<Integer> enemies;
    }

    private static final List<BiFunction<PhysicsBuilder, EntityManager, BaseEnemy>> enemies = new ArrayList<>(
            List.of((pB, eM) -> new BasicSlime(pB, eM)));

    public class LevelData {
        public String startRoom;
        public RandomCollection<String> fillerRoomsCollection;
        public RandomCollection<String> leafRoomsCollection;
        public String endRoom;
        public BaseEnemy[] enemies;

        public LevelData(JsonData data, String directory) {
            this.startRoom = maketmx(data.start);
            this.fillerRoomsCollection = buildCollection(data.filler);
            this.leafRoomsCollection = buildCollection(data.leaf);
            this.endRoom = maketmx(data.end);
            this.enemies = new BaseEnemy[data.enemies.size()];
            int i = 0;
            for (int enemyIndex : data.enemies) {
                this.enemies[i++] = DungeonData.enemies.get(enemyIndex).apply(physicsBuilder, entityManager);
            }
        }

        private RandomCollection<String> buildCollection(HashMap<String, Double> map) {
            RandomCollection<String> collection = new RandomCollection<>(random);
            map.forEach((key, value) -> {
                collection.add(value, maketmx(key));
            });
            return collection;
        }
    }

    private Random random;
    private PhysicsBuilder physicsBuilder;
    private EntityManager entityManager;
    private AtlasManager atlasManager;
    private AssetManager assetManager;
    private Json json = new Json();
    private String directory;

    public DungeonData(PhysicsBuilder physicsBuilder, EntityManager entityManager,
            AtlasManager atlasManager,
            AssetManager assetManager) {
        this.random = Globals.random;
        this.physicsBuilder = physicsBuilder;
        this.entityManager = entityManager;
        this.atlasManager = atlasManager;
        this.assetManager = assetManager;
    }

    private String maketmx(String name) {
        return directory + name + ".tmx";
    }

    private void loadTileMap(String name) {
        this.assetManager.load(maketmx(name), TiledMap.class);
    }

    private void loadTileMaps(JsonData data) {
        loadTileMap(data.start);
        data.filler.forEach((key, value) -> {
            loadTileMap(key);
        });
        data.leaf.forEach((key, value) -> {
            loadTileMap(key);
        });
        loadTileMap(data.end);
    }

    private void loadEntity(String name) {
        this.atlasManager.add("entities/" + name + "/" + name + ".atlas");
    }

    public LevelData load(String level) {
        this.directory = "levels/" + level + "/";
        FileHandle fileHandle = Gdx.files.internal(directory + "levelData.json");
        JsonData data = json.fromJson(JsonData.class, fileHandle);
        LevelData levelData = new LevelData(data, directory);

        loadEntity("Player");
        this.atlasManager.add("weapons/" + Globals.equippedWeapon + "/" + Globals.equippedWeapon + ".atlas");
        this.atlasManager.add("levels/" + level + "/levelSprites.atlas");
        loadTileMaps(data);

        for (BaseEnemy enemy : levelData.enemies) {
            loadEntity(enemy.getClass().getSimpleName());
        }

        return levelData;
    }
}
