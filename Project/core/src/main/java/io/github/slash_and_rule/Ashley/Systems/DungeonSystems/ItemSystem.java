package io.github.slash_and_rule.Ashley.Systems.DungeonSystems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ressources;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.ItemComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;

public class ItemSystem extends IteratingSystem {
    public ItemSystem() {
        super(Family.all(ItemComponent.class, SensorComponent.class).get(), Globals.Priorities.Systems.Dungeon.Item);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SensorComponent sensor = entity.getComponent(SensorComponent.class);
        for (SensorComponent.CollisionData data : sensor.contactsStarted) {
            if (data.otherFixture.getFilterData().categoryBits != Globals.Categories.Player) {
                continue;
            }
            ItemComponent itemComponent = entity.getComponent(ItemComponent.class);
            Ressources.addItem(itemComponent.item, 1);
            getEngine().removeEntity(entity);
            return;
        }
    }
}
