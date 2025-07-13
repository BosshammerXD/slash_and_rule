package io.github.slash_and_rule.Bases;

import java.util.Arrays;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Affine2;

import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.Mappers;

public abstract class SpriteRenderSystem<T extends RenderableComponent> extends BaseRenderSystem {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private AtlasManager atlasManager;
    private ComponentMapper<T> renderMapper;

    public SpriteRenderSystem(OrthographicCamera camera, AtlasManager atlasManager, Class<T> renderComp, int priority) {
        super(Family.all(renderComp, TransformComponent.class).exclude(InactiveComponent.class).get(),
                priority); // Assuming AtlasManager is not needed here
        this.batch = new SpriteBatch();
        this.camera = camera;
        this.atlasManager = atlasManager;
        this.renderMapper = ComponentMapper.getFor(renderComp);
    }

    @Override
    public void update(float deltaTime) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        Entity[] sorted = renderEntities.toArray(Entity.class);
        sortZ(sorted);
        for (Entity entity : sorted) {
            renderEntity(entity);
        }
        batch.end();
    }

    protected void renderEntity(Entity entity) {
        RenderableComponent renderable = renderMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);

        if (renderable.dirty) {
            Arrays.sort(renderable.textures);
            renderable.dirty = false;
        }

        for (TextureData textureData : renderable.textures) {
            if (textureData.texture == null) {
                if (textureData.name == null || textureData.atlasPath == null) {
                    continue;
                }
                textureData.texture = atlasManager.getTexture(textureData.atlasPath, textureData.name);
            }

            boolean hasNaN = Float.isNaN(textureData.width) || Float.isNaN(textureData.height)
                    || Float.isNaN(textureData.offsetX)
                    || Float.isNaN(textureData.offsetY);
            float width;
            if (Float.isNaN(textureData.width) || hasNaN) {
                width = textureData.texture.getRegionWidth() * textureData.scale;
            } else {
                width = textureData.width;
            }
            float height;
            if (Float.isNaN(textureData.height) || hasNaN) {
                height = textureData.texture.getRegionHeight() * textureData.scale;
            } else {
                height = textureData.height;
            }
            float offsetX;
            if (Float.isNaN(textureData.offsetX)) {
                offsetX = -width / 2f;
            } else if (hasNaN) {
                offsetX = -width / 2f + textureData.offsetX;
            } else {
                offsetX = textureData.offsetX;
            }

            float offsetY;
            if (Float.isNaN(textureData.offsetY)) {
                offsetY = -height / 2f;
            } else if (hasNaN) {
                offsetY = -height / 2f + textureData.offsetY;
            } else {
                offsetY = textureData.offsetY;
            }

            Affine2 transformMatrix = new Affine2().rotate(textureData.angle)
                    .preTranslate(transform.position.x, transform.position.y)
                    .translate(offsetX, offsetY);
            batch.draw(textureData.texture, width, height, transformMatrix);
        }
    }
}
