package io.github.slash_and_rule.Ashley.Systems.CitySystems;

import java.util.HashMap;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.slash_and_rule.Ressources;
import io.github.slash_and_rule.Utils.AtlasManager;

public class ResourceSystem extends EntitySystem {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Camera camera;
    private HashMap<String, TextureRegion> resourceTextures = new HashMap<>();
    private AtlasManager atlasManager;

    public ResourceSystem(Camera camera, AtlasManager atlasManager, int priority) {
        super(priority);
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.camera = camera;
        this.atlasManager = atlasManager;
    }

    @Override
    public void removedFromEngine(Engine engine) {
        resourceTextures.clear();
    }

    BitmapFont font = new BitmapFont();
    GlyphLayout layout = new GlyphLayout();

    @Override
    public void update(float deltaTime) {
        if (resourceTextures.isEmpty()) {
            for (String resource : Ressources.ALL_RESSOURCES) {
                TextureRegion texture = atlasManager.getTexture("ressources/ressources.atlas", resource);
                if (texture != null) {
                    resourceTextures.put(resource, texture);
                } else {
                    System.err.println("Resource texture not found: " + resource);
                }
            }
        }

        float x = camera.position.x - camera.viewportWidth / 2;
        float y = camera.position.y + camera.viewportHeight / 2;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.41f, 0.355f, 0.275f, 1f);
        shapeRenderer.rect(x, y - 32, camera.viewportWidth, 32);
        shapeRenderer.end();

        float offsetX = 0;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (String name : Ressources.ALL_RESSOURCES) {
            TextureRegion texture = resourceTextures.get(name);
            batch.draw(texture, x + offsetX, y - 24);
            offsetX += texture.getRegionWidth();
            int amount = Ressources.items.getOrDefault(name, new Ressources.ItemData(name)).amount;
            layout.setText(font, ": " + amount);
            font.draw(batch, ": " + amount, x + offsetX, y - 8);
            offsetX += layout.width + 8; // Add some space after the amount
        }
        batch.end();
    }
}
