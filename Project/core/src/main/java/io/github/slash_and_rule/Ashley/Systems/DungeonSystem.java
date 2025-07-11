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
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.slash_and_rule.Ashley.Builder.PhysCompBuilder;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.EnemyComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Bases.BaseEnemy;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonRoom;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.EnemyPicker;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomEntity;
import io.github.slash_and_rule.Interfaces.CollisionHandler;
import io.github.slash_and_rule.Utils.Mappers;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonBuilder;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.LoadingScreen;

public class DungeonSystem extends EntitySystem {
    private RoomEntity room;
    private RoomEntity[] neighbours = new RoomEntity[4]; // 0: left, 1: down, 2: right, 3: up

    private ArrayDeque<Runnable> schedule = new ArrayDeque<>();

    private Engine engine;
    private ImmutableArray<Entity> players;

    private DungeonManager dungeonManager;
    private DungeonBuilder dungeonBuilder;

    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer mapRenderer;

    private PhysCompBuilder physCompBuilder;
    private EnemyPicker enemyPicker;

    private ArrayDeque<BaseEnemy> enemies = new ArrayDeque<>();
    private ImmutableArray<Entity> enemyEntities;
    private float time = 0f;

    public DungeonSystem(int priority, DungeonManager dungeonManager, PhysCompBuilder physicsBuilder,
            OrthographicCamera camera, float scale) {
        super(priority);
        this.physCompBuilder = physicsBuilder;
        this.dungeonManager = dungeonManager;
        this.camera = camera;
        this.mapRenderer = new OrthogonalTiledMapRenderer(null, scale);
        dungeonManager.setOnDungeonGenerated(this::init);
        this.enemyPicker = new EnemyPicker();
    }

    @Override
    public void update(float deltaTime) {
        camera.update();
        if (!this.schedule.isEmpty()) {
            schedule.pop().run();
        }
        if (this.mapRenderer.getMap() != null) {
            this.mapRenderer.setView(camera);
            this.mapRenderer.render();
        }
        for (Entity enemy : enemyEntities) {
            EnemyComponent enemyComponent = Mappers.enemyMapper.get(enemy);
            PhysicsComponent physicsComponent = Mappers.physicsMapper.get(enemy);
            if (enemyComponent.startPos != null) {
                physicsComponent.body.setTransform(enemyComponent.startPos, 0);
                enemyComponent.startPos = null;
            }
        }
        if (!enemies.isEmpty()) {
            if (time >= Globals.spawnInterval) {
                time = 0f;
                spawnEntity();
            } else {
                time += deltaTime;
            }
        }
        if (enemyEntities.size() == 0 && enemies.isEmpty() && room != null
                && !dungeonManager.getRoom().cleared) {
            dungeonManager.getRoom().cleared = true;
            room.setOpen(true, neighbours);
        }
    }

    // region: Entity spawning
    private void spawnEntity() {
        Vector2[] spawners = room.getSpawners();
        for (Vector2 spawner : spawners) {
            if (enemies.isEmpty()) {
                return;
            }
            if (spawner == null || checkPlayersToClose(spawner)) {
                continue;
            }

            BaseEnemy enemy = enemies.pop();
            enemy.makeEntity(spawner);
            return; // Spawn one enemy at a time
        }
    }

    private boolean checkPlayersToClose(Vector2 spawner) {
        for (Entity player : players) {
            TransformComponent transformComponent = Mappers.transformMapper.get(player);
            if (transformComponent == null) {
                System.out.println("DungeonSystem: Player does not have a TransformComponent");
                continue;
            }
            if (spawner.cpy().sub(transformComponent.position).len() < Globals.spawnDistance) {
                return true;
            }
        }
        return false;
    }
    // endregion

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = engine;
        this.players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        this.enemyEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        this.dungeonBuilder = new DungeonBuilder(physCompBuilder);
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

    private void addRoomEntity(RoomEntity roomEntity, int direction) {
        if (roomEntity == null || roomEntity.entity == null) {
            return;
        }
        if (direction < 0) {
            if (room != null) {
                removeRoomEntity(-1);
            }
            room = roomEntity;
        } else if (direction < 4) {
            if (neighbours[direction] != null) {
                removeRoomEntity(direction);
            }
            neighbours[direction] = roomEntity;
        } else {
            return;
        }
        roomEntity.add(engine);
    }

    private void removeRoomEntity(int direction) {
        RoomEntity roomEntity = null;
        if (direction < 0 && room != null) {
            roomEntity = room;
            room = null;
        } else if (direction < 4 && neighbours[direction] != null) {
            roomEntity = neighbours[direction];
            neighbours[direction] = null;
        }
        if (roomEntity == null) {
            return;
        }
        roomEntity.remove(engine);
    }

    private void change_room(int direction) {
        schedule.clear();
        // Set the current room inactive first
        room.setActive(false);
        room.setOpen(false, neighbours); // close doors so when we move back, the doors are closed

        // shift the new room to the current room and the old room to the correct
        // neighbour
        if (neighbours[(direction + 2) % 4] != null)
            removeRoomEntity((direction + 2) % 4);
        neighbours[(direction + 2) % 4] = room;
        room = neighbours[direction];
        neighbours[direction] = null;
        dungeonManager.move(direction);

        // Now activate the new room (after the reference swap)
        room.setActive(true);
        // clear the rest of the neighbours
        removeRoomEntity((direction + 1) % 4);
        removeRoomEntity((direction + 3) % 4);
        // set the doors
        boolean open = dungeonManager.getRoom().cleared || room.getSpawners().length == 0;
        room.setOpen(open, neighbours);
        if (!open) {
            this.time = Globals.spawnInterval;
            this.enemies = enemyPicker.pickEnemies(dungeonManager.getRoom().difficulty * 10);
        }
        // schedule the new rooms for Generation
        DungeonRoom dungeonRoom = dungeonManager.getRoom();
        scheduleRoom(direction, dungeonRoom.neighbours[direction]);
        scheduleRoom((direction + 1) % 4, dungeonRoom.neighbours[(direction + 1) % 4]);
        scheduleRoom((direction + 3) % 4, dungeonRoom.neighbours[(direction + 3) % 4]);

        // teleport the player to the new room
        teleportPlayer((direction + 2) % 4);

        // set the new room as active
        setMap();
    }

    private void scheduleRoom(int direction, DungeonRoom room) {
        if (neighbours[direction] != null)
            removeRoomEntity(direction);
        dungeonManager.getData(room, roomData -> {
            dungeonBuilder.scheduledMakeRoom(schedule, roomData, room.neighbours, new DoorHandler(), entity -> {
                addRoomEntity(entity, direction);
                this.room.roomFinishedLoading(direction);
            });
        });
    }

    private void teleportPlayer(int direction) {
        for (Entity player : players) {
            PhysicsComponent physicsComponent = Mappers.physicsMapper.get(player);
            TransformComponent transformComponent = Mappers.transformMapper.get(player);
            if (physicsComponent == null || transformComponent == null) {
                System.out.println("DungeonSystem: Player does not have a PhysicsComponent or TransformComponent");
                continue;
            }

            // Get the target spawn point
            Vector2 spawnPoint = room.getSpawnPoint(direction);
            if (spawnPoint == null) {
                System.out.println("DungeonSystem: No spawn point found for direction: " + direction);
                continue;
            }

            // Clear any existing velocity before teleporting
            physicsComponent.body.setLinearVelocity(0, 0);
            physicsComponent.body.setAngularVelocity(0);

            // Teleport the player to the new room
            physicsComponent.body.setTransform(spawnPoint, 0f);

            // Update transform component to match new physics position
            transformComponent.position.set(spawnPoint);
            transformComponent.lastPosition.set(spawnPoint);

            System.out.println("DungeonSystem: Teleported player to new room at " + spawnPoint);
        }
    }

    private void setMap() {
        this.mapRenderer.setMap(room.getTileMap());
    }

    public void init(LoadingScreen loader) {
        enemyPicker.setEnemies(dungeonManager.level.enemies);
        DungeonRoom myRoom = dungeonManager.getRoom();
        DungeonRoom other = myRoom.neighbours[1];

        dungeonManager.getData(myRoom, loader, roomdata -> {
            this.room = dungeonBuilder.makeRoom(roomdata, myRoom.neighbours, new DoorHandler());
            addRoomEntity(room, -1);
            room.setActive(true);
            room.setOpen(true, neighbours);
            this.mapRenderer.setMap(roomdata.map);
        });

        dungeonManager.getData(other, loader, roomdata2 -> {
            neighbours[1] = dungeonBuilder.makeRoom(roomdata2, other.neighbours, new DoorHandler());
            addRoomEntity(neighbours[1], 1);
            room.roomFinishedLoading(1);
        });
    }
}
