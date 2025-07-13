package io.github.slash_and_rule.Ashley.Systems.DungeonSystems;

import java.util.function.Consumer;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Signals;
import io.github.slash_and_rule.Ashley.Components.ChildComponent;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.StateComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DoorComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class DoorSystem extends EntitySystem {
    private Consumer<Integer> moveFunc;
    private ImmutableArray<Entity> doors;

    public DoorSystem(Consumer<Integer> moveFunc, int priority) {
        super(priority);

        this.moveFunc = moveFunc;

        Signals.roomOpenSignal.add((signal, event) -> {
            Entity roomEntity = event.roomEntity;
            for (Entity door : doors) {
                DoorComponent doorComp = Mappers.doorMapper.get(door);
                ChildComponent childComp = Mappers.childMapper.get(door);
                if (doorComp == null || childComp == null || childComp.parent != roomEntity) {
                    continue;
                }
                doorComp.open = DoorComponent.DoorState.OPENING;
            }
        });
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        doors = engine.getEntitiesFor(Family.all(
                DoorComponent.class,
                ChildComponent.class,
                PhysicsComponent.class,
                SensorComponent.class).exclude(InactiveComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity door : doors) {
            DoorComponent doorComp = Mappers.doorMapper.get(door);
            PhysicsComponent physComp = Mappers.physicsMapper.get(door);
            SensorComponent sensorComp = Mappers.sensorMapper.get(door);
            for (SensorComponent.CollisionData data : sensorComp.contactsStarted) {
                handleCollision(data, door, doorComp, door);
                return;
            }
            switch (doorComp.open) {
                case DoorComponent.DoorState.CLOSING:
                    setDoor(physComp, (short) 0, Globals.WallMask);
                    doorComp.open = DoorComponent.DoorState.ClOSED;
                    break;
                case DoorComponent.DoorState.OPENING:
                    if (doorComp.neighbour == null) {
                        continue;
                    }
                    setDoor(physComp, Globals.PlayerCategory, (short) 0);
                    doorComp.open = DoorComponent.DoorState.OPEN;
                    break;
                default:
                    break;
            }
        }
    }

    private void handleCollision(SensorComponent.CollisionData data, Entity door, DoorComponent doorComp,
            Entity entity) {
        // TODO: change if two player added
        if (!data.myFixture.isSensor()) {
            return;
        }
        Entity player = data.entity;
        Entity neighbour = doorComp.neighbour;
        Entity myRoom = Mappers.childMapper.get(entity).parent;

        int direction = doorComp.type.value;
        moveFunc.accept(direction);

        changeRoom(door, myRoom, neighbour);

        teleportPlayer(player, doorComp, neighbour);
    }

    private void teleportPlayer(Entity player, DoorComponent doorComp, Entity room) {
        PhysicsComponent physicsComponent = Mappers.physicsMapper.get(player);
        TransformComponent transformComponent = Mappers.transformMapper.get(player);
        if (physicsComponent == null || transformComponent == null) {
            return;
        }

        int direction = (doorComp.type.value + 2) % 4; // Adjust direction to match room's orientation
        Vector2 spawnPoint = Mappers.dungeonMapper.get(room).spawnPoints.get(direction);

        if (spawnPoint == null) {
            System.out.println(Mappers.dungeonMapper.get(room).spawnPoints.toString());
            spawnPoint = new Vector2(2, 2);
        }

        // Clear any existing velocity before teleporting
        physicsComponent.body.setLinearVelocity(0, 0);
        physicsComponent.body.setAngularVelocity(0);

        // Teleport the player to the new room
        physicsComponent.body.setTransform(spawnPoint, 0f);

        // Update transform component to match new physics position
        transformComponent.position.set(spawnPoint);
        transformComponent.lastPosition.set(spawnPoint);
    }

    private void changeRoom(Entity door, Entity myRoom, Entity neighbour) {
        StateComponent myStateComp = Mappers.stateMapper.get(myRoom);
        StateComponent neighbourStateComp = Mappers.stateMapper.get(neighbour);
        if (myStateComp == null || neighbourStateComp == null) {
            System.out.println("DoorSystem: Missing StateComponent for room entities");
            return;
        }
        myStateComp.state = StateComponent.State.DEACTIVATE;
        neighbourStateComp.state = StateComponent.State.ACTIVATE;
    }

    private void setDoor(PhysicsComponent physComp, short sensorMask, short wallMask) {
        Fixture sensor = physComp.fixtures.get("Sensor");
        Filter filter = sensor.getFilterData();
        filter.maskBits = sensorMask;
        sensor.setFilterData(filter);

        Fixture wall = physComp.fixtures.get("Wall");
        Filter wallFilter = wall.getFilterData();
        wallFilter.maskBits = wallMask;
        wall.setFilterData(wallFilter);
    }
}
