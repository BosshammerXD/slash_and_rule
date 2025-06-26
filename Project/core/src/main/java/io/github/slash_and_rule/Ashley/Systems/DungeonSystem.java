package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonRoom;
import io.github.slash_and_rule.Interfaces.CollisionHandler;
import io.github.slash_and_rule.Utils.Mappers;
import io.github.slash_and_rule.Utils.PhysicsBuilder;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonBuilder;
import io.github.slash_and_rule.LoadingScreen;

public class DungeonSystem extends EntitySystem {
    private Entity room;
    private Entity[] neighbours = new Entity[4]; // 0: left, 1: down, 2: right, 3: up

    private ArrayDeque<Runnable> schedule = new ArrayDeque<>();
    private boolean[] doorswaiting = new boolean[4]; // 0: left, 1: down, 2: right, 3: up

    private Engine engine;
    private ImmutableArray<Entity> players;

    private DungeonManager dungeonManager;
    private DungeonBuilder dungeonBuilder;

    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer mapRenderer;

    public DungeonSystem(int priority, DungeonManager dungeonManager, PhysicsBuilder physicsBuilder,
            OrthographicCamera camera, float scale) {
        super(priority);
        this.dungeonManager = dungeonManager;
        this.dungeonBuilder = new DungeonBuilder(physicsBuilder);
        this.camera = camera;
        this.mapRenderer = new OrthogonalTiledMapRenderer(null, scale);
        dungeonManager.setOnDungeonGenerated(this::init);
    }

    @Override
    public void update(float deltaTime) {
        if (!this.schedule.isEmpty()) {
            schedule.pop().run();
        }
        if (this.mapRenderer.getMap() != null) {
            this.mapRenderer.setView(camera);
            this.mapRenderer.render();
        }
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = engine;
        this.players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    private void room_finished_loading(int direction) {
        if (doorswaiting[direction]) {
            doorswaiting[direction] = false;
            set_open_door(direction, true);
        }
    }

    private void set_open_door(int direction, boolean open) {
        Entity neighbour = neighbours[direction];
        if (neighbour == null) {
            doorswaiting[direction] = open;
            return;
        }
        DungeonComponent dungeonComponent = Mappers.dungeonMapper.get(room);
        if (dungeonComponent == null) {
            System.out.println("DungeonSystem: Room does not have a DungeonComponent");
            return;
        }
        Fixture[] doorComponents = dungeonComponent.doors[direction];
        if (doorComponents == null) {
            System.out.println("no door in Direction: " + direction);
            return;
        }

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
                return;
            }
            int doorId = (Integer) myFixtureUserData;

            schedule.addFirst(() -> change_room(doorId));

        }
    }

    private void clear_room(int direction) {
        Entity neighbour = neighbours[direction];
        if (neighbour == null) {
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
        if (neighbours[(direction + 2) % 4] != null)
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
        DungeonRoom dungeonRoom = dungeonManager.getRoom();
        scheduleRoom(direction, dungeonRoom.neighbours[direction]);
        scheduleRoom((direction + 1) % 4, dungeonRoom.neighbours[(direction + 1) % 4]);
        scheduleRoom((direction + 3) % 4, dungeonRoom.neighbours[(direction + 3) % 4]);

        // teleport the player to the new room
        teleportPlayer((direction + 2) % 4);

        // set the new room as active
        set_room_active(room, true);
        setMap();
    }

    private void scheduleRoom(int direction, DungeonRoom room) {
        if (neighbours[direction] != null)
            engine.removeEntity(neighbours[direction]);
        dungeonManager.getData(room, roomData -> {
            dungeonBuilder.scheduledMakeRoom(schedule, roomData, room.neighbours, new DoorHandler(), entity -> {
                neighbours[direction] = entity;
                room_finished_loading(direction);
            });
        });
    }

    private void teleportPlayer(int direction) {
        for (Entity player : players) {
            PhysicsComponent physicsComponent = Mappers.physicsMapper.get(player);
            TransformComponent transformComponent = Mappers.transformMapper.get(player);
            if (physicsComponent == null || transformComponent == null) {
                System.out.println("DungeonSystem: Player does not have a PhysicsComponent");
                continue;
            }
            // Teleport the player to the new room
            Vector2 oldPos = new Vector2(physicsComponent.body.getPosition());
            physicsComponent.body.setTransform(
                    Mappers.dungeonMapper.get(room).spawnPoints[direction],
                    0f);
            transformComponent.position.set(physicsComponent.body.getPosition());
            transformComponent.lastPosition.add(physicsComponent.body.getPosition()).sub(oldPos);
        }
    }

    private void setMap() {
        this.mapRenderer.setMap(Mappers.dungeonMapper.get(room).map);
    }

    public void init(LoadingScreen loader) {
        DungeonRoom myRoom = dungeonManager.getRoom();
        DungeonRoom other = myRoom.neighbours[1];

        // Load both rooms in parallel to avoid nested callback timing issues
        dungeonManager.getData(myRoom, loader, roomdata -> {
            this.room = dungeonBuilder.makeRoom(roomdata, myRoom.neighbours, new DoorHandler());
            set_room_active(room, true);
            set_open_doors(true);
            this.mapRenderer.setMap(roomdata.map);
        });

        dungeonManager.getData(other, loader, roomdata2 -> {
            neighbours[1] = dungeonBuilder.makeRoom(roomdata2, other.neighbours, new DoorHandler());
            room_finished_loading(1);
        });
    }
}
