package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayList;
import java.util.Comparator;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

import io.github.slash_and_rule.Ashley.Components.TransformComponent;

public class SortedRenderSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private final ArrayList<Entity> sortedEntities = new ArrayList<>();
    private final Comparator<Entity> zComparator;

    public SortedRenderSystem() {
        zComparator = (a, b) -> Float.compare(
                a.getComponent(TransformComponent.class).z,
                b.getComponent(TransformComponent.class).z);
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class).get());
    }

    @Override
    public void update(float delta) {
        sortedEntities.clear();
        sortedEntities.addAll(entities.toArray());
        sortedEntities.sort(zComparator);

        batch.begin();
        for (Entity e : sortedEntities) {
            // Rendering-Logik hier
        }
        batch.end();
    }
}
