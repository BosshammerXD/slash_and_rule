package io.github.slash_and_rule.Bases;

import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.Interfaces.Displayable;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.LoadingScreen.LoadingSchedule;
import io.github.slash_and_rule.LoadingScreen.MsgRunnable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public abstract class TileMapObject extends BaseGameObject implements Displayable, Initalizable {
    protected TiledMap map;
    protected OrthogonalTiledMapRenderer renderer;
    private String mapFilePath;
    protected float scale;

    public TileMapObject(BaseScreen screen, InputManager inputManager, String mapFilePath, float scale) {
        super(screen, inputManager);
        // Constructor logic here

        screen.drawableObjects.add(this);
        screen.loadableObjects.add(this);

        this.mapFilePath = mapFilePath;
        this.scale = scale;
    }

    @Override
    public void init(LoadingSchedule loader) {
        // Initialize the map and renderer when the object is initialized
        loader.assetManager.load(mapFilePath, TiledMap.class);
        loader.todo.add(new MsgRunnable("loading Tilemaps", () -> loadMap(loader.assetManager)));
    }

    private void loadMap(AssetManager assetManager) {
        // Load the map from the asset manager
        map = assetManager.get(mapFilePath, TiledMap.class);
        renderer = new OrthogonalTiledMapRenderer(map, scale);
    }

    @Override
    public void show(AssetManager assetManager) {
        // Initialize the map and renderer when the screen is shown
        map = assetManager.get(mapFilePath, TiledMap.class);
        renderer = new OrthogonalTiledMapRenderer(map, scale);

        // Load collision objects from the map if necessary
    }

    @Override
    public void dispose() {
        map.dispose();
        if (renderer != null) {
            renderer.dispose();
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (renderer != null) {
            renderer.setView(screen.camera);
            renderer.render();
        }
    }

    @Override
    public void hide() {
        // Logic to hide the object, if necessary
    }

}
