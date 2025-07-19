package io.github.slash_and_rule.Ashley.Systems.RenderSystems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.OrthographicCamera;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Bases.SpriteRenderSystem;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.Mappers;

public class MFRenderSystem extends SpriteRenderSystem<MidfieldComponent> {

    public MFRenderSystem(OrthographicCamera camera, AtlasManager atlasManager) {
        super(camera, atlasManager, MidfieldComponent.class, Globals.Priorities.Systems.Draw.Midfield);
    }

    @Override
    protected float zFunction(Entity entity) {
        return Mappers.transformMapper.get(entity).z - Mappers.transformMapper.get(entity).position.y;
    }
}
