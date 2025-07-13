package io.github.slash_and_rule.Ashley.Systems.CitySystems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CityBGSystem extends EntitySystem {
    private SpriteBatch batch = new SpriteBatch();
    private Camera gameCamera;
    private Texture grassTexture;

    public CityBGSystem(Camera gameCamera, AssetManager assetManager, int priority) {
        super(priority);
        this.gameCamera = gameCamera;
        this.grassTexture = assetManager.get("city/Grass.png", Texture.class);
        this.grassTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }

    @Override
    public void update(float deltaTime) {
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();

        // Zeichne die Textur über den gesamten Viewport
        float viewportWidth = gameCamera.viewportWidth;
        float viewportHeight = gameCamera.viewportHeight;

        // Positioniere die Textur basierend auf der Kamera-Position
        float cameraX = gameCamera.position.x - viewportWidth / 2;
        float cameraY = gameCamera.position.y - viewportHeight / 2;

        batch.draw(grassTexture, cameraX, cameraY, viewportWidth, viewportHeight, 0, 0,
                viewportWidth / grassTexture.getWidth() * 32, viewportHeight / grassTexture.getHeight() * 32);

        batch.end();
    }
}
