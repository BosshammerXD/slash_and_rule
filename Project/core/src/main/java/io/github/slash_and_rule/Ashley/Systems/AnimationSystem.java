package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Animations.BaseAnimation;
import io.github.slash_and_rule.Ashley.Components.AnimatedComponent;
import io.github.slash_and_rule.Ashley.Components.RenderableComponent;

public class AnimationSystem extends IteratingSystem {
    private ComponentMapper<AnimatedComponent> animatedMapper = ComponentMapper.getFor(AnimatedComponent.class);
    private ComponentMapper<RenderableComponent> renderableMapper = ComponentMapper.getFor(RenderableComponent.class);

    public AnimationSystem(int priority) {
        super(Family.all(AnimatedComponent.class, RenderableComponent.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AnimatedComponent animated = animatedMapper.get(entity);
        RenderableComponent renderable = renderableMapper.get(entity);

        if (animated == null || renderable == null) {
            System.out.println("Entity does not have the required components for animation processing.");
            return; // Skip if the entity does not have the required components
        }
        for (AnimatedComponent.AnimationData animationData : animated.animations) {
            BaseAnimation animation = animationData.animation;
            if (animation == null) {
                System.out.println("Animation data is null for texture index: " + animationData.textureIndex);
                continue; // Skip if the animation is null
            }
            int index = animationData.textureIndex;
            if (index < 0 || index >= renderable.textures.length) {
                System.out.println("Invalid texture index: " + index + " for entity: " + entity);
                continue; // Skip if the texture index is out of bounds
            }

            animation.update(deltaTime); // Update the animation frame
            renderable.textures[index].texture = animation.getFrame();
        }
    }
}
