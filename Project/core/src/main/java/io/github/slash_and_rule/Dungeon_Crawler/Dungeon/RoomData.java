package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.ArrayDeque;
import java.util.function.Consumer;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;

import io.github.slash_and_rule.Bases.BaseScreen;
import io.github.slash_and_rule.Interfaces.AsyncLoadable;

public class RoomData implements AsyncLoadable {
    public static class ColliderData {
        public float x, y, width, height;

        public ColliderData(Rectangle rect) {
            this.width = rect.width;
            this.height = rect.height;
            this.x = rect.x + rect.width;
            this.y = rect.y + rect.height;

        }
    }

    public static class DoorData {
        public ColliderData collider;
        public ColliderData sensor;
        public float[] spawnPoint = new float[2]; // x, y
        public String type;

        public DoorData(Rectangle rect, String type) {
            this.collider = new ColliderData(rect);
            modifySensorRect(rect, type);
            this.sensor = new ColliderData(rect);
            setSpawnPoint(rect, type);
            this.type = type;
        }

        private void modifySensorRect(Rectangle rect, String type) {
            if (type.equals("left")) {
                rect.x -= rect.width;
            } else if (type.equals("right")) {
                rect.x += rect.width;
            } else if (type.equals("top")) {
                rect.y += rect.height;
            } else if (type.equals("bottom")) {
                rect.y -= rect.height;
            }
        }

        private void setSpawnPoint(Rectangle rect, String type) {
            float x, y;
            if (type.equals("left")) {
                x = rect.x + rect.width * 2 + 1f;
                y = rect.y + rect.height;
            } else if (type.equals("right")) {
                x = rect.x - rect.width * 2;
                y = rect.y + rect.height;
            } else if (type.equals("top")) {
                x = rect.x + rect.width;
                y = rect.y - rect.height * 2;
            } else if (type.equals("bottom")) {
                x = rect.x + rect.width;
                y = rect.y + rect.height * 2 + 1f;
            } else {
                x = 0;
                y = 0;
            }
            spawnPoint[0] = x;
            spawnPoint[1] = y;
        }
    }

    public static class UtilData {
        public float x, y, width, height;
        public String type;

        public UtilData(Rectangle rect, String type) {
            this.x = rect.x;
            this.y = rect.y;
            this.width = rect.width;
            this.height = rect.height;
            this.type = type;
        }

        public UtilData(Ellipse elli, String type) {
            this.x = elli.x - elli.width;
            this.y = elli.y - elli.height;
            this.width = elli.width * 2;
            this.height = elli.height * 2;
            this.type = type;
        }
    }

    public static float scale = 1f;

    private boolean done = false;

    public TiledMap map;
    public ColliderData[] walls;
    public UtilData[] utils;
    public DoorData[] doors;

    private AssetManager assetManager;
    private String mapFilePath;

    private ArrayDeque<Consumer<RoomData>> callBack = new ArrayDeque<>();

    public RoomData(BaseScreen screen, String mapFilePath, AssetManager assetManager) {
        this.mapFilePath = mapFilePath;
        this.assetManager = assetManager;
        screen.loadAsset(mapFilePath, TiledMap.class);

        screen.asyncLoadableObjects.add(this);
    }

    public RoomData(BaseScreen screen, String mapFilePath, AssetManager assetManager, Consumer<RoomData> callback) {
        this(screen, mapFilePath, assetManager);
        this.callBack.add(callback);
    }

    @Override
    public AsyncResult<AsyncLoadable> schedule(AsyncExecutor asyncExecutor) {
        return asyncExecutor.submit(() -> {
            map = assetManager.get(mapFilePath, TiledMap.class);
            genWallData();
            genDoorData();
            genUtilData();
            return this;
        });
    }

    @Override
    public void loadDone() {
        this.done = true;
        while (!callBack.isEmpty()) {
            Consumer<RoomData> callback = callBack.poll();
            if (callback != null) {
                callback.accept(this);
            }
        }
    }

    public void addCallback(Consumer<RoomData> callback) {
        if (done) {
            callback.accept(this);
        } else {
            callBack.add(callback);
        }
    }

    public boolean isDone() {
        return done;
    }

    private void genWallData() {
        MapObjects objects = map.getLayers().get("collision").getObjects();
        ArrayDeque<ColliderData> wallStack = new ArrayDeque<>();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject rectangleObject) {
                wallStack.add(new ColliderData(getRect(rectangleObject)));
            }
        }

        walls = wallStack.toArray(new ColliderData[0]);
    }

    private void genDoorData() {
        MapObjects objects = map.getLayers().get("door").getObjects();
        ArrayDeque<DoorData> doorColliderStack = new ArrayDeque<>();

        for (MapObject object : objects) {
            String type = object.getProperties().get("type", String.class);
            if (object instanceof RectangleMapObject rectangleObject && type != null) {
                doorColliderStack.add(new DoorData(getRect(rectangleObject), type));
            }
        }

        doors = doorColliderStack.toArray(new DoorData[0]);
    }

    private void genUtilData() {
        if (map.getLayers().get("util") == null) {
            utils = new UtilData[0];
            return;
        }
        MapObjects objects = map.getLayers().get("util").getObjects();
        ArrayDeque<UtilData> utilStack = new ArrayDeque<>();
        for (MapObject object : objects) {
            String type = object.getProperties().get("type", String.class);
            if (object instanceof RectangleMapObject rectangleObject) {
                utilStack.add(new UtilData(getRect(rectangleObject), type));
            } else if (object instanceof EllipseMapObject circleObject) {
                utilStack.add(new UtilData(getElli(circleObject), type));
            }
        }

        utils = utilStack.toArray(new UtilData[0]);
    }

    private Rectangle getRect(RectangleMapObject rectObject) {
        Rectangle rect = rectObject.getRectangle();
        rect.x *= scale;
        rect.y *= scale;
        rect.width *= scale / 2f;
        rect.height *= scale / 2f;
        return rect;
    }

    private Ellipse getElli(EllipseMapObject elliObject) {
        Ellipse elli = elliObject.getEllipse();
        elli.x *= scale;
        elli.y *= scale;
        elli.width *= scale / 2f;
        elli.height *= scale / 2f;
        return elli;
    }
}
