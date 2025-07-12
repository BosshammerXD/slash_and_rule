package io.github.slash_and_rule.Bases;

import java.util.Arrays;
import java.util.Comparator;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

public abstract class BaseRenderSystem extends EntitySystem {
    protected ImmutableArray<Entity> renderEntities;
    protected Family renderFamily;

    public BaseRenderSystem(Family renderFamily, int priority) {
        super(priority);

        this.renderFamily = renderFamily;
    }

    @Override
    public void addedToEngine(Engine engine) {
        renderEntities = engine.getEntitiesFor(renderFamily);
    }

    @Override
    public void removedFromEngine(Engine engine) {
    }

    protected void sortZ(Entity[] entities) {
        Arrays.sort(entities, Comparator.comparing(this::zFunction));
    }

    protected abstract float zFunction(Entity entity);
}
