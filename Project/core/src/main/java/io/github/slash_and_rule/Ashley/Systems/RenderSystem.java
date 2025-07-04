package io.github.slash_and_rule.Ashley.Systems;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Affine2;

import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.BackgroundComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.ForegroundComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.Mappers;

public class RenderSystem extends EntitySystem {
    private ImmutableArray<Entity> backgroundEntities;
    private ImmutableArray<Entity> midfieldEntities;
    private ImmutableArray<Entity> foregroundEntities;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private AtlasManager atlasManager;

    public RenderSystem(int priority, OrthographicCamera camera, AtlasManager atlasManager) {
        super(priority);

        this.batch = new SpriteBatch();
        this.camera = camera;
        this.atlasManager = atlasManager;
    }

    @Override
    public void addedToEngine(Engine engine) {
        backgroundEntities = engine.getEntitiesFor(
                Family.all(TransformComponent.class, RenderableComponent.class, BackgroundComponent.class).get());

        midfieldEntities = engine.getEntitiesFor(
                Family.all(TransformComponent.class, RenderableComponent.class, MidfieldComponent.class).get());

        foregroundEntities = engine.getEntitiesFor(
                Family.all(TransformComponent.class, RenderableComponent.class, ForegroundComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
    }

    @Override
    public void update(float deltaTime) {

        // camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Render background entities
        renderEntities(backgroundEntities, e -> Mappers.transformMapper.get(e).z);
        // Render midfield entities
        renderEntities(midfieldEntities, e -> {
            TransformComponent transform = Mappers.transformMapper.get(e);
            return transform.z + transform.position.y;
        });
        // Render foreground entities
        renderEntities(foregroundEntities, e -> Mappers.transformMapper.get(e).z);
        batch.end();
    }

    private void sortZ(Entity[] entities, Function<Entity, Float> zFunction) {
        Arrays.sort(entities, Comparator.comparing(zFunction));
    }

    private void renderEntity(Entity entities) {
        RenderableComponent renderable = Mappers.renderableMapper.get(entities);
        TransformComponent transform = Mappers.transformMapper.get(entities);

        for (Integer key : renderable.textures.keySet()) {
            for (TextureData textureData : renderable.textures.get(key)) {
                if (textureData.texture == null) {
                    if (textureData.name == null || textureData.atlasPath == null) {
                        continue;
                    }
                    textureData.texture = atlasManager.getTexture(textureData.atlasPath, textureData.name);
                }

                Affine2 transformMatrix = new Affine2().rotate(textureData.angle)
                        .preTranslate(transform.position.x, transform.position.y)
                        .translate(textureData.offsetX, textureData.offsetY);

                batch.draw(textureData.texture,
                        textureData.width, textureData.height,
                        transformMatrix);
            }
        }
    }

    private void renderEntities(ImmutableArray<Entity> entities, Function<Entity, Float> zFunction) {
        Entity[] sorted = entities.toArray(Entity.class);
        sortZ(sorted, zFunction);

        for (Entity entity : sorted) {
            renderEntity(entity);
        }
    }
}
