package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Animations.AnimData;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.AnimatedComponent;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.Mappers;

public class AnimationSystem extends IteratingSystem {

    private AtlasManager atlasManager;

    public AnimationSystem(AtlasManager atlasManager) {
        super(
                Family.all(AnimatedComponent.class).exclude(InactiveComponent.class).get(),
                Globals.AnimationSystemPriority);

        this.atlasManager = atlasManager;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AnimatedComponent animatedComponent = Mappers.animatedMapper.get(entity);
        for (AnimData animData : animatedComponent.animations.values()) {
            animData.update(deltaTime, entity);
            String atlasPath = animData.getAtlasPath();
            String name = animData.getName();
            if (atlasPath == null || name == "" || animData.getIndex() < 0) {
                animData.setTexture(null);
                continue;
            }
            TextureRegion[] anim = atlasManager.getAnimation(atlasPath, name);
            if (anim == null || anim.length == 0) {
                animData.setTexture(null);
                continue;
            }
            animData.setTexture(anim[animData.getIndex()]);
        }
    }
}
