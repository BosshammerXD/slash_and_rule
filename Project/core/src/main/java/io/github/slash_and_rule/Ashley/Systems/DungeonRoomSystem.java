package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Ashley.Signals;
import io.github.slash_and_rule.Ashley.Builder.PhysCompBuilder;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.ParentComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.StateComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DoorComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.EnemyComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Bases.BaseEnemy;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonBuilder;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonManager;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.DungeonRoom;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.EnemyPicker;
import io.github.slash_and_rule.Utils.Mappers;
import io.github.slash_and_rule.Utils.QuadData;

public class DungeonRoomSystem extends EntitySystem {
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private DungeonManager dungeonManager;
    private DungeonBuilder dungeonBuilder;
    private ArrayDeque<Runnable> schedule = new ArrayDeque<>();

    private EnemyPicker enemyPicker = new EnemyPicker();
    private ArrayDeque<BaseEnemy> enemies = new ArrayDeque<>();
    private float timeSinceLastSpawn = 0f;

    private ImmutableArray<Entity> rooms;
    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> enemyEntities;

    public DungeonRoomSystem(PhysCompBuilder physCompBuilder, DungeonManager dungeonManager, int priority,
            OrthographicCamera camera) {
        super(priority);
        this.dungeonBuilder = new DungeonBuilder(physCompBuilder);
        this.dungeonManager = dungeonManager;
        this.mapRenderer = new OrthogonalTiledMapRenderer(null, 1 / 32f);
        this.camera = camera;
    }

    @Override
    public void addedToEngine(Engine engine) {
        rooms = engine.getEntitiesFor(Family.all(
                DungeonComponent.class, ParentComponent.class).get());
        players = engine.getEntitiesFor(Family.all(
                PhysicsComponent.class, PlayerComponent.class).exclude(InactiveComponent.class).get());
        enemyEntities = engine.getEntitiesFor(Family.all(
                PhysicsComponent.class, EnemyComponent.class).exclude(InactiveComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        if (!schedule.isEmpty()) {
            schedule.pop().run();
        }

        for (Entity room : rooms) {
            processEntity(room, deltaTime);
        }
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    private void processEntity(Entity entity, float deltaTime) {
        DungeonComponent dungeonComp = Mappers.dungeonMapper.get(entity);
        StateComponent stateComp = Mappers.stateMapper.get(entity);
        if (stateComp.stateChanged) {
            ArrayDeque<Entity> doors = getDoors(entity);
            switch (stateComp.state) {
                case StateComponent.State.ACTIVE:
                    active(doors, entity);
                    if (dungeonManager.getRoom().cleared || dungeonComp.spawnerPositions.length == 0) {
                        Signals.roomOpenSignal.dispatch(new Signals.RoomOpenEvent(entity));
                        dungeonComp.cleared = true;
                    } else {
                        this.enemies = enemyPicker.pickEnemies(dungeonManager.getRoom().difficulty * 10);
                        this.timeSinceLastSpawn = Globals.spawnInterval;
                    }
                    break;
                case StateComponent.State.INACTIVE:
                    inactive(doors);
                    break;
                default:
                    break;
            }
        }
        if (Mappers.inactiveMapper.has(entity)) {
            return;
        }
        mapRenderer.setMap(dungeonComp.map);

        if (!dungeonComp.cleared && spawnEnemies(dungeonComp, deltaTime) && enemyEntities.size() == 0) {
            dungeonComp.cleared = true;
            Signals.roomOpenSignal.dispatch(new Signals.RoomOpenEvent(entity));
        }
    }

    private ArrayDeque<Entity> getDoors(Entity entity) {
        ParentComponent parentComp = Mappers.parentMapper.get(entity);
        ArrayDeque<Entity> neighbours = new ArrayDeque<>();
        for (Entity child : parentComp.children) {
            if (Mappers.doorMapper.has(child)) {
                neighbours.add(child);
            }
        }
        return neighbours;
    }

    private void inactive(ArrayDeque<Entity> doors) {
        for (Entity door : doors) {
            DoorComponent doorComp = Mappers.doorMapper.get(door);
            Entity neighbour = doorComp.neighbour;
            if (neighbour == null) {
                continue;
            }
            if (Mappers.inactiveMapper.has(neighbour)) {
                getEngine().removeEntity(neighbour);
                doorComp.neighbour = null;
            }
        }
    }

    private void active(ArrayDeque<Entity> doors, Entity myEntity) {
        for (Entity door : doors) {
            final DoorComponent doorComp = Mappers.doorMapper.get(door);
            Entity neighbour = doorComp.neighbour;
            if (neighbour == null) {
                int dir = doorComp.type.value;
                DungeonRoom room = dungeonManager.getRoom().neighbours.get(dir);
                QuadData<?> neighbours = room.neighbours;

                dungeonManager.getData(dungeonManager.getRoom().neighbours.get(dir),
                        data -> dungeonBuilder.scheduledMakeRoom(schedule, data, (dir + 2) % 4, myEntity,
                                room.difficulty * 10, neighbours, entity -> {
                                    getEngine().addEntity(entity);
                                    doorComp.neighbour = entity;
                                }));

            }
        }
    }

    public void init(LoadingScreen screen, final DungeonRoom dungeon) {
        enemyPicker.setEnemies(dungeonManager.level.enemies);
        dungeonManager.getData(dungeon, screen, data -> {
            Entity mainRoom = dungeonBuilder.makeRoom(data, dungeon.neighbours, dungeon.difficulty * 10);
            StateComponent stateComp = Mappers.stateMapper.get(mainRoom);
            stateComp.state = StateComponent.State.ACTIVATE;
            getEngine().addEntity(mainRoom);
        });
    }

    private boolean spawnEnemies(DungeonComponent dungeonComp, float deltaTime) {
        if (enemies.isEmpty()) {
            return true;
        }
        if (timeSinceLastSpawn < Globals.spawnInterval) {
            timeSinceLastSpawn += deltaTime;
            return false;
        }
        timeSinceLastSpawn = 0f;

        Vector2[] spawners = dungeonComp.spawnerPositions;

        for (int i = spawners.length; i > 0; i--) {
            int index = Globals.random.nextInt(0, i);
            Vector2 spawner = spawners[index];

            if (spawner == null || checkToClose(spawner, players) || checkToClose(spawner, enemyEntities)) {
                spawners[index] = spawners[i - 1];
                spawners[i - 1] = spawner;
                continue;
            }

            BaseEnemy enemy = enemies.pop();
            enemy.makeEntity(spawner);
            return false;
        }
        return false;
    }

    private boolean checkToClose(Vector2 position, ImmutableArray<Entity> entities) {
        for (Entity entity : entities) {
            PhysicsComponent physComp = Mappers.physicsMapper.get(entity);
            if (physComp == null || physComp.body == null) {
                continue;
            }
            Vector2 entityPosition = Mappers.physicsMapper.get(entity).body.getPosition();
            if (entityPosition.dst2(position) < Globals.spawnDistance2) {
                return true;
            }
        }
        return false;
    }
}
