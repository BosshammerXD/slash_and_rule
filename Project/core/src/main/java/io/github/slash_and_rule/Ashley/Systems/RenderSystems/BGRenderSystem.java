package io.github.slash_and_rule.Ashley.Systems.RenderSystems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.slash_and_rule.Ashley.Components.DrawingComponents.BackgroundComponent;
import io.github.slash_and_rule.Bases.SpriteRenderSystem;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.Mappers;

public class BGRenderSystem extends SpriteRenderSystem<BackgroundComponent> {
    private ExtendViewport viewport;

    public BGRenderSystem(ExtendViewport viewport, OrthographicCamera camera, AtlasManager atlasManager, int priority) {
        super(camera, atlasManager, BackgroundComponent.class, priority);
        this.viewport = viewport;
    }

    @Override
    protected float zFunction(Entity entity) {
        return Mappers.transformMapper.get(entity).z;
    }

    @Override
    public void update(float deltaTime) {
        viewport.apply();
        super.update(deltaTime);
    }
}
