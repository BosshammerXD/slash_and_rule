package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.function.Consumer;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class RoomEntity {
    private boolean[] doorsWaiting = { false, false, false, false }; // 0: left, 1: down, 2: right, 3: up

    public Entity entity;
    private Vector2[] spawnPoints; // 0: left, 1: down, 2: right, 3: up
    private TiledMap tilemap;
    public Entity[] utilEntities;
    private Vector2[] spawners;

    private <T extends Component> void applyToComponent(ComponentMapper<T> mapper, Entity entity,
            Consumer<T> callback) {
        T component = mapper.get(entity);
        if (component != null) {
            callback.accept(component);
        }
    }

    private Consumer<PhysicsComponent> physicsActive(boolean active) {
        return physicsComponent -> physicsComponent.body.setActive(active);
    }

    public RoomEntity(Entity entity, Vector2[] spawnPoints, TiledMap tilemap, Entity[] utilEntities,
            Vector2[] spawners) {
        if (entity == null || spawnPoints == null || tilemap == null || utilEntities == null) {
            throw new IllegalArgumentException("Entity, spawnPoints, tilemap, and utilEntities cannot be null");
        } else if (spawnPoints.length != 4) {
            throw new IllegalArgumentException("Spawn points must have exactly 4 elements (left, down, right, up)");
        }
        this.entity = entity;
        this.spawnPoints = spawnPoints;
        this.tilemap = tilemap;
        this.utilEntities = utilEntities;
        this.spawners = spawners;
    }

    public void add(Engine engine) {
        engine.addEntity(entity);
        for (Entity utilEntity : utilEntities) {
            engine.addEntity(utilEntity);
        }
    }

    public void remove(Engine engine) {
        engine.removeEntity(entity);
        for (Entity utilEntity : utilEntities) {
            engine.removeEntity(utilEntity);
        }
    }

    public void setActive(boolean active) {
        Consumer<PhysicsComponent> physicsActive = physicsActive(active);
        applyToComponent(Mappers.physicsMapper, entity, physicsActive);
        for (Entity utilEntity : utilEntities) {
            applyToComponent(Mappers.physicsMapper, utilEntity, physicsActive);
        }
        applyToComponent(Mappers.renderableMapper, entity, comp -> {
            for (TextureData textureData : comp.textures) {
                textureData.atlasPath = (active) ? "levels/level_1/levelSprites.atlas" : null;
                if (!active) {
                    textureData.texture = null;
                    System.out.println("RoomEntity: Texture set to null for inactive room");
                }
            }
        });
    }

    public void roomFinishedLoading(int direction) {
        if (direction < 0 || direction >= doorsWaiting.length) {
            throw new IllegalArgumentException("Direction must be between 0 and " + (doorsWaiting.length - 1));
        }
        if (doorsWaiting[direction]) {
            doorsWaiting[direction] = false;
            setOpenDoor(direction, true);
        }
    }

    public void setOpen(boolean open, Object[] neighbours) {
        if (neighbours == null || neighbours.length != 4) {
            throw new IllegalArgumentException("Neighbours must have exactly 4 elements (left, down, right, up)");
        }
        for (int i = 0; i < 4; i++) {
            if (neighbours[i] == null) {
                doorsWaiting[i] = open;
            } else {
                setOpenDoor(i, open);
            }
        }
    }

    private void setOpenDoor(int direction, boolean open) {
        DungeonComponent dungeonComponent = Mappers.dungeonMapper.get(entity);
        if (dungeonComponent == null) {
            System.out.println("RoomEntity: Entity does not have a DungeonComponent");
            return;
        }
        Fixture[] doorComponents = dungeonComponent.doors[direction];
        if (doorComponents == null) {
            System.out.println("RoomEntity: No door in Direction: " + direction);
            return;
        }
        short filter1 = open ? Globals.PlayerCategory : (short) 0;
        short filter2 = open ? (short) (Globals.ItemCategory | Globals.HitboxCategory) : Globals.WallMask;
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

    private void set_door_filter(Fixture fixture, short maskBits) {
        if (fixture == null) {
            System.out.println("RoomEntity: Fixture is null");
            return;
        }
        Filter filter = fixture.getFilterData();
        filter.maskBits = maskBits;
        fixture.setFilterData(filter);
    }

    public TiledMap getTileMap() {
        return tilemap;
    }

    public Vector2 getSpawnPoint(int direction) {
        if (direction < 0 || direction >= spawnPoints.length) {
            throw new IllegalArgumentException("Direction must be between 0 and " + (spawnPoints.length - 1));
        }
        return spawnPoints[direction];
    }

    public Vector2[] getSpawners() {
        return spawners;
    }
}