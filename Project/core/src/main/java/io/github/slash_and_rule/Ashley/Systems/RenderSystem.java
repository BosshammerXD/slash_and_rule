package io.github.slash_and_rule.Ashley.Systems;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.BackgroundComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.ForegroundComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;

public class RenderSystem extends EntitySystem {
    private ImmutableArray<Entity> backgroundEntities;
    private ImmutableArray<Entity> midfieldEntities;
    private ImmutableArray<Entity> foregroundEntities;

    private ComponentMapper<RenderableComponent> rm = ComponentMapper.getFor(RenderableComponent.class);
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);

    private SpriteBatch batch;
    private OrthographicCamera camera;

    public RenderSystem(int priority, OrthographicCamera camera) {
        super(priority);

        this.batch = new SpriteBatch();
        this.camera = camera;
    }

    @Override
    public void addedToEngine(Engine engine) {
        backgroundEntities = engine.getEntitiesFor(
                Family.all(TransformComponent.class, BackgroundComponent.class).get());

        midfieldEntities = engine.getEntitiesFor(
                Family.all(TransformComponent.class, MidfieldComponent.class).get());

        foregroundEntities = engine.getEntitiesFor(
                Family.all(TransformComponent.class, ForegroundComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
    }

    @Override
    public void update(float deltaTime) {

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // Render background entities
        renderEntities(backgroundEntities, e -> tm.get(e).z);
        // Render midfield entities
        renderEntities(midfieldEntities, e -> {
            TransformComponent transform = tm.get(e);
            return transform.z + transform.position.y;
        });
        // Render foreground entities
        renderEntities(foregroundEntities, e -> tm.get(e).z);
        batch.end();
    }

    private void sortZ(Entity[] entities, Function<Entity, Float> zFunction) {
        Arrays.sort(entities, Comparator.comparing(zFunction));
    }

    private void renderEntity(Entity entities) {
        RenderableComponent renderable = rm.get(entities);
        TransformComponent transform = tm.get(entities);

        for (RenderableComponent.TextureData textureData : renderable.textures) {
            batch.draw(textureData.texture,
                    transform.position.x + textureData.offsetX,
                    transform.position.y + textureData.offsetY,
                    textureData.width, textureData.height);
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
