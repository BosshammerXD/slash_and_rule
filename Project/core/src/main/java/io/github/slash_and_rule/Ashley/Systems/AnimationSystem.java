package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.Mappers;

public class AnimationSystem extends IteratingSystem {

    private AtlasManager atlasManager;

    public AnimationSystem(int priority, AtlasManager atlasManager) {
        super(Family.all(RenderableComponent.class).get(), priority);

        this.atlasManager = atlasManager;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        RenderableComponent renderable = Mappers.renderableMapper.get(entity);
        if (renderable == null || renderable.textures == null) {
            return;
        }
        for (RenderableComponent.TextureData textureData : renderable.textures) {
            RenderableComponent.AnimData animData = textureData.animData;
            if (animData == null) {
                continue;
            }
            animData.updater.accept(animData, deltaTime);
            TextureRegion[] anim = atlasManager.getAnimation(animData.atlasPath, animData.name);
            if (animData.animIndex > anim.length - 1) {
                animData.animIndex %= anim.length;
            }
            textureData.texture = anim[animData.animIndex];
        }
    }
}
