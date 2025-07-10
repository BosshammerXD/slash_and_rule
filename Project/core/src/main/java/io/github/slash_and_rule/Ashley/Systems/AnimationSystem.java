package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.slash_and_rule.Animations.AnimData;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
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
        for (TextureData textureData : renderable.getAllTextures()) {
            AnimData animData = textureData.animData;
            if (animData == null) {
                continue;
            }
            animData.update(deltaTime, entity);
            String atlasPath = animData.getAtlasPath();
            String name = animData.getName();
            if (atlasPath == null || name == "" || animData.animIndex < 0) {
                textureData.texture = null;
                continue;
            }
            TextureRegion[] anim = atlasManager.getAnimation(atlasPath, name);
            if (anim == null || anim.length == 0) {
                textureData.texture = null;
                continue;
            }
            if (animData.animIndex < 0) {
                textureData.texture = null;
                continue;
            }
            textureData.texture = anim[animData.animIndex];
        }
    }
}
