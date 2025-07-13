package io.github.slash_and_rule.Ashley.Systems.CitySystems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import io.github.slash_and_rule.Ressources;
import io.github.slash_and_rule.Ashley.Signals;
import io.github.slash_and_rule.Ashley.Builder.CompBuilders;
import io.github.slash_and_rule.Ashley.Builder.RenderBuilder;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.CityComponents.BuildingComponent;
import io.github.slash_and_rule.Ashley.Components.CityComponents.BuyableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Systems.InputSystem.MouseInputType;
import io.github.slash_and_rule.CityBuilder.CityData;
import io.github.slash_and_rule.Ressources.ItemData;
import io.github.slash_and_rule.Utils.Mappers;

public class BuildingSystem extends EntitySystem {
    private ImmutableArray<Entity> buildings;
    private ImmutableArray<Entity> input;
    private Camera camera;
    private Runnable func;

    public BuildingSystem(Camera camera, Runnable func, int priority) {
        super(priority);
        this.camera = camera;
        this.func = func;
    }

    private RenderBuilder<MidfieldComponent> renderBuilder = new RenderBuilder<>();

    private void onPlaceBuilding(Signal<Signals.PlaceBuildingEvent> signal, Signals.PlaceBuildingEvent event) {
        if (getEngine() == null) {
            System.out.println("BuildingSystem not added to engine yet.");
            return;
        }

        Entity buildingEntity = event.buildingEntity;

        TransformComponent transform = Mappers.transformMapper.get(buildingEntity);

        Vector3 position = new Vector3(transform.position.x + 0.5f, transform.position.y + 0.5f, 0);

        if (getBuilding(position) != null) {
            CityData.heldEntity = null;
            return;
        }

        BuyableComponent buyable = buildingEntity.getComponent(BuyableComponent.class);

        final boolean[] canAfford = new boolean[] { true };
        buyable.cost.forEach((resource, amount) -> {
            ItemData item = Ressources.items.get(resource);
            if (item == null) {
                canAfford[0] = false;
                return;
            }
            if (item.amount < amount) {
                canAfford[0] = false;
                return;
            }
        });
        if (!canAfford[0]) {
            System.out.println("Nicht genug Ressourcen, um das Gebäude zu platzieren.");
            CityData.heldEntity = null;
            return;
        }
        buyable.cost.forEach((resource, amount) -> {
            ItemData item = Ressources.items.get(resource);
            item.amount -= amount;
        });

        TransformComponent buildingTransform = buildingEntity.getComponent(TransformComponent.class);
        Vector2 pos = buildingTransform.position;

        BuildingComponent buildingComp = buildingEntity.getComponent(BuildingComponent.class);

        Entity entity = new Entity();
        entity.add(CompBuilders.buildTransform(pos, 0).get());
        entity.add(buildingComp.copy());

        renderBuilder.begin(new MidfieldComponent());
        for (MidfieldComponent.TextureData data : Mappers.foregroundMapper.get(buildingEntity).textures) {
            MidfieldComponent.TextureData copy = data.copy();
            renderBuilder.add(copy);
        }
        renderBuilder.end(entity);
        getEngine().addEntity(entity);

        getEngine().removeEntity(buildingEntity);
        CityData.heldEntity = null;

        CityData.buildings.add(entity);

        System.out.println("Gebäude platziert: " + buildingEntity);
    }

    @Override
    public void addedToEngine(Engine engine) {
        System.out.println("BuildingSystem added to engine" + this.getEngine());
        Signals.placeBuildingSignal.add(this::onPlaceBuilding);
        buildings = engine.getEntitiesFor(
                Family.all(BuildingComponent.class, TransformComponent.class, MidfieldComponent.class).get());
        input = engine.getEntitiesFor(Family.all(PlayerComponent.class, ControllableComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        System.out.println("BuildingSystem removed from engine");
        Signals.placeBuildingSignal.remove(this::onPlaceBuilding);
    }

    @Override
    public void update(float deltaTime) {
        for (Entity player : input) {
            ControllableComponent controllable = player.getComponent(ControllableComponent.class);
            if (controllable == null || CityData.heldEntity != null)
                continue;

            for (ControllableComponent.MouseData data : controllable.mouseQueue) {
                if (data.type != MouseInputType.DOWN || data.button != Input.Buttons.LEFT) {
                    continue;
                }

                Vector3 mousePos = new Vector3(data.screenX, data.screenY, 0);
                camera.unproject(mousePos);

                Entity building = getBuilding(mousePos);

                if (building == null) {
                    continue;
                }
                BuildingComponent buildingComp = Mappers.buildingMapper.get(building);
                if (buildingComp.name == "Castle") {
                    func.run();
                    return;
                }
            }
        }
    }

    private Entity getBuilding(Vector3 mousePos) {
        for (Entity building : buildings) {
            TransformComponent transform = building.getComponent(TransformComponent.class);
            if (transform == null)
                continue;

            Vector3 buildingPos = new Vector3(transform.position.x + 0.5f, transform.position.y + 0.5f, 0);

            if (buildingPos.dst2(mousePos) < 0.5f * 0.5f) { // Assuming a radius of 0.5 for the building
                return building;
            }
        }
        return null;
    }
}
