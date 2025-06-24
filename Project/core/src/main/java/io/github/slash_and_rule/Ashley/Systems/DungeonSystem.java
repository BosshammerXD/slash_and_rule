package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonRoom;
import io.github.slash_and_rule.Interfaces.CollisionHandler;
import io.github.slash_and_rule.Utils.Mappers;

public class DungeonSystem extends EntitySystem {
    private Entity room;
    private Entity[] neighbours = new Entity[4]; // 0: left, 1: down, 2: right, 3: up

    private ArrayDeque<Runnable> schedule = new ArrayDeque<>();
    private boolean[] doorswaiting = new boolean[4]; // 0: left, 1: down, 2: right, 3: up

    private Engine engine;

    private DungeonManager dungeonManager;

    public DungeonSystem(int priority, DungeonManager dungeonManager) {
        super(priority);
        this.dungeonManager = dungeonManager;
        // TODO
    }

    @Override
    public void update(float deltaTime) {
        // TODO
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = engine;
    }

    private void room_finished_loading(int direction) {
        if (doorswaiting[direction]) {
            doorswaiting[direction] = false;
            set_open_door(direction, true);
        }
    }

    private void set_open_door(int direction, boolean open) {
        Entity neighbour = neighbours[direction];
        if (neighbour == null && open) {
            doorswaiting[direction] = true;
            return;
        }
        DungeonComponent dungeonComponent = Mappers.dungeonMapper.get(room);
        if (dungeonComponent == null) {
            System.out.println("DungeonSystem: Room does not have a DungeonComponent");
            return;
        }
        Fixture[] doorComponents = dungeonComponent.doors[direction];
        short filter1 = open ? Globals.PlayerCategory : (short) 0;
        short filter2 = open ? (short) (Globals.ItemCategory | Globals.ProjectileCategory) : Globals.WallMask;
        for (Fixture doorComponent : doorComponents) {
            if (doorComponent == null) {
                continue;
            }
            if (doorComponent.getUserData() instanceof Integer) {
                set_door_filter(doorComponent, filter1);
            } else {
                set_door_filter(doorComponent, filter2);
            }
        }
    }

    private void set_open_doors(boolean open) {
        for (int i = 0; i < 4; i++) {
            set_open_door(i, open);
        }
    }

    private void set_door_filter(Fixture fixture, short maskBits) {
        if (fixture == null) {
            System.out.println("DungeonSystem: Fixture is null");
            return;
        }
        Filter filter = fixture.getFilterData();
        filter.maskBits = maskBits;
        fixture.setFilterData(filter);
    }

    private void set_room_active(Entity room, boolean active) {
        if (room == null) {
            System.out.println("DungeonSystem: Room is null");
            return;
        }
        PhysicsComponent physicsComponent = Mappers.physicsMapper.get(room);
        if (physicsComponent == null) {
            System.out.println("DungeonSystem: Room does not have a PhysicsComponent");
            return;
        }
        physicsComponent.body.setActive(active);
    }

    private class DoorHandler implements CollisionHandler {
        @Override
        public void handleCollision(Entity myEntity, Fixture myFixture, Entity otherEntity, Fixture otherFixture) {
            Object myFixtureUserData = myFixture.getUserData();
            if (!(myFixtureUserData instanceof Integer)) {
                System.out.println("DungeonSystem: myFixtureUserData is not an Integer");
                return;
            }
            int doorId = (Integer) myFixtureUserData;

            schedule.addFirst(() -> change_room(doorId));

        }
    }

    private void clear_room(int direction) {
        Entity neighbour = neighbours[direction];
        if (neighbour == null) {
            System.out.println("DungeonSystem: No neighbour in direction " + direction);
            return;
        }
        engine.removeEntity(neighbour);
        neighbours[direction] = null;
    }

    private void change_room(int direction) {
        schedule.clear();
        // Set the current room inactive and the new room active
        set_room_active(room, false);
        set_open_doors(false); // close doors so when we move back, the doors are closed
        set_room_active(neighbours[direction], true);
        // shift the new room to the current room and the old room to the correct
        // neighbour
        engine.removeEntity(neighbours[(direction + 2) % 4]);
        neighbours[(direction + 2) % 4] = room;
        room = neighbours[direction];
        neighbours[direction] = null;
        dungeonManager.move(direction);
        // clear the rest of the neighbours
        clear_room((direction + 1) % 4);
        clear_room((direction + 3) % 4);
        // set the doors
        set_open_doors(dungeonManager.getRoom().cleared);
        // schedule the new rooms for Generation
    }
}
