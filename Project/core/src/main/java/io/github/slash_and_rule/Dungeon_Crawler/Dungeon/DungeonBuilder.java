package io.github.slash_and_rule.Dungeon_Crawler.Dungeon;

import java.util.ArrayDeque;
import java.util.function.Consumer;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Builder.CompBuilders;
import io.github.slash_and_rule.Ashley.Builder.PhysCompBuilder;
import io.github.slash_and_rule.Ashley.Builder.RenderBuilder;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.ParentComponent;
import io.github.slash_and_rule.Ashley.Components.StateComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.BackgroundComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DoorComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DungeonComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.EntryComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.DoorComponent.DoorType;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.ColliderData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.DoorData;
import io.github.slash_and_rule.Dungeon_Crawler.Dungeon.RoomData.UtilData;
import io.github.slash_and_rule.Utils.QuadData;
import io.github.slash_and_rule.Utils.ShapeBuilder;

public class DungeonBuilder {
    private PhysCompBuilder physicsCompBuilder;
    private RenderBuilder<BackgroundComponent> renderBuilder = new RenderBuilder<BackgroundComponent>();

    public DungeonBuilder(PhysCompBuilder physCompBuilder) {
        this.physicsCompBuilder = physCompBuilder;
    }

    public Entity makeRoom(RoomData data, QuadData<?> neighbours, int balance) {
        final ArrayDeque<Entity> children = new ArrayDeque<>();
        final ArrayDeque<Vector2> spawnerPositions = new ArrayDeque<>();

        final Entity entity = new Entity();
        final DungeonComponent dungeonComponent = new DungeonComponent();
        final PhysicsComponent physicsComponent = physicsCompBuilder.make(BodyType.StaticBody, 0f, false);

        final int[] index = { 0 }; // Used to keep track of wall indices
        final QuadData<Vector2> spawnPoints = new QuadData<>();

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            buildWall(wall, physicsComponent, index);
        }

        renderBuilder.begin(new BackgroundComponent());
        for (DoorData door : data.doors) {
            makeDoor(door, neighbours, physicsComponent, index, spawnPoints, children, -2, null);
        }
        renderBuilder.end(entity);

        for (UtilData util : data.utils) {
            if (util.type.equals("entry")) {
                children.add(makeEntry(util));
            } else if (util.type.equals("spawner")) {
                spawnerPositions.add(new Vector2(util.x, util.y));
            } else if (util.type.equals("chest")) {
                children.add(makeTreasure(util));
            }
        }

        dungeonComponent.spawnPoints = getSpawnPoints(spawnPoints);
        dungeonComponent.spawnerPositions = spawnerPositions.toArray(new Vector2[0]);
        dungeonComponent.map = data.map;
        dungeonComponent.balance = balance;

        final ParentComponent parentComponent = new ParentComponent();
        parentComponent.children = children.toArray(new Entity[0]);

        final StateComponent stateComponent = new StateComponent();
        stateComponent.state = StateComponent.State.INACTIVE;

        EntityManager.build(entity,
                physicsComponent,
                dungeonComponent,
                new TransformComponent(),
                parentComponent,
                stateComponent,
                new InactiveComponent());

        return entity;
    }

    public void scheduledMakeRoom(ArrayDeque<Runnable> schedule, RoomData data, int direction, Entity origin,
            int balance, QuadData<?> neighbours, Consumer<Entity> onFinish) {
        final ArrayDeque<Entity> children = new ArrayDeque<>();
        final ArrayDeque<Vector2> spawnerPositions = new ArrayDeque<>();

        final Entity entity = new Entity();
        final DungeonComponent dungeonComponent = new DungeonComponent();
        final PhysicsComponent physicsComponent = physicsCompBuilder.make(BodyType.StaticBody, 0f, false);

        final int[] index = { 0 }; // Used to keep track of wall indices
        final QuadData<Vector2> spawnPoints = new QuadData<>();

        for (int i = 0; i < data.walls.length; i++) {
            ColliderData wall = data.walls[i];
            schedule.add(() -> buildWall(wall, physicsComponent, index));
        }

        schedule.add(() -> {
            renderBuilder.begin(new BackgroundComponent());
            for (DoorData door : data.doors) {
                makeDoor(door, neighbours, physicsComponent, index, spawnPoints, children, direction, origin);
            }
            renderBuilder.end(entity);
        });

        for (UtilData util : data.utils) {
            if (util.type.equals("entry")) {
                schedule.add(() -> children.add(makeEntry(util)));
            } else if (util.type.equals("spawner")) {
                schedule.add(() -> spawnerPositions.add(new Vector2(util.x, util.y)));
            } else if (util.type.equals("chest")) {
                schedule.add(() -> children.add(makeTreasure(util)));
            }
        }

        schedule.add(() -> {
            dungeonComponent.spawnPoints = getSpawnPoints(spawnPoints);
            dungeonComponent.spawnerPositions = spawnerPositions.toArray(new Vector2[0]);
            dungeonComponent.map = data.map;
            dungeonComponent.balance = balance;
        });

        schedule.add(() -> {
            final ParentComponent parentComponent = new ParentComponent();
            parentComponent.children = children.toArray(new Entity[0]);
            final StateComponent stateComponent = new StateComponent();
            stateComponent.state = StateComponent.State.INACTIVE;
            EntityManager.build(entity,
                    physicsComponent,
                    dungeonComponent,
                    new TransformComponent(),
                    parentComponent,
                    stateComponent,
                    new InactiveComponent());
        });

        schedule.add(() -> {
            if (onFinish != null) {
                onFinish.accept(entity);
            }
        });
    }

    private DoorType dirToDoorType(String direction) {
        switch (direction.toLowerCase()) {
            case "left":
                return DoorType.LEFT;
            case "bottom":
                return DoorType.BOTTOM;
            case "right":
                return DoorType.RIGHT;
            case "top":
                return DoorType.TOP;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    private Fixture buildWall(ColliderData wall, PhysicsComponent comp, int[] index) {
        Shape shape = ShapeBuilder.rect(wall.x, wall.y, wall.width * 2f, wall.height * 2f);
        return physicsCompBuilder.asyncAdd(comp, "wall_" + index[0]++, shape, Globals.Categories.Wall,
                Globals.Masks.Wall,
                false);
    }

    private void addWallSprite(ColliderData wall, DoorType doorType) {
        String spriteName;
        float width = wall.width * 2;
        float height = wall.height * 2;
        float x = wall.x - wall.width;
        float y = wall.y - wall.height;
        switch (doorType) {
            case DoorType.LEFT:
                spriteName = "Dungeon_Wall_Left";
                break;
            case DoorType.BOTTOM:
                spriteName = "Dungeon_Wall_Bottom";
                break;
            case DoorType.RIGHT:
                spriteName = "Dungeon_Wall_Right";
                break;
            case DoorType.TOP:
                spriteName = "Dungeon_Wall_Top";
                height *= 2;
                break;
            default:
                return;
        }
        renderBuilder.add("levels/" + Globals.Dungeon.Level + "/levelSprites.atlas", spriteName, 0, width, height, x,
                y);
    }

    private void makeDoor(DoorData door, QuadData<?> neighbours, PhysicsComponent comp, int[] i,
            QuadData<Vector2> spawnPoints, ArrayDeque<Entity> children, int dir, Entity origin) {
        // TODO: Add the picture of the door
        DoorType doorType = dirToDoorType(door.type);
        Object neighbour = neighbours.get(doorType.value);
        if (neighbour == null) {
            addWallSprite(door.collider, doorType);
            buildWall(door.collider, comp, i);
            return;
        }

        children.add(buildDoor(door, dirToDoorType(door.type), i, dir, origin));

        spawnPoints.set(doorType.value, new Vector2(door.spawnPoint[0], door.spawnPoint[1]));
    }

    private Entity buildDoor(DoorData door, DoorType doorType, int[] i, int dir, Entity origin) {
        Entity entity = new Entity();

        PhysicsComponent physComp = physicsCompBuilder.make(BodyType.StaticBody, 0f, false);

        ColliderData wall = door.collider;
        Shape shape = ShapeBuilder.rect(wall.x, wall.y, wall.width * 2f, wall.height * 2f);
        physicsCompBuilder.asyncAdd(physComp, "Wall", shape, Globals.Categories.Wall, Globals.Masks.Wall, false);

        ColliderData doorSensor = door.sensor;
        Shape doorShape = ShapeBuilder.rect(doorSensor.x, doorSensor.y, doorSensor.width * 2f, doorSensor.height * 2f);
        physicsCompBuilder.asyncAdd(physComp, "Sensor", doorShape, Globals.Categories.Sensor, (short) 0, true);

        StateComponent stateComp = new StateComponent();
        stateComp.state = StateComponent.State.INACTIVE;

        DoorComponent doorComp = new DoorComponent();
        doorComp.type = doorType;
        if (doorType.value == dir) {
            doorComp.neighbour = origin;
        }

        EntityManager.build(entity, physComp, new SensorComponent(), stateComp, new InactiveComponent(), doorComp);

        return entity;
    }

    private Entity makeEntry(UtilData entry) {
        // TODO
        Entity entity = new Entity();
        Shape shape = ShapeBuilder.circ(entry.x + entry.width, entry.y + entry.height, entry.width);

        physicsCompBuilder.begin(BodyType.StaticBody, 0f, false);
        physicsCompBuilder.add("Sensor", shape, Globals.Categories.Sensor, Globals.Categories.Player, true);

        StateComponent stateComp = new StateComponent();
        stateComp.state = StateComponent.State.INACTIVE;

        RenderBuilder<MidfieldComponent> renderBuilder = new RenderBuilder<>();

        renderBuilder.begin(new MidfieldComponent());
        renderBuilder.add("levels/" + Globals.Dungeon.Level + "/levelSprites.atlas", "Dungeon_Portal", 0, 1 / 16f);
        renderBuilder.end(entity);

        TransformComponent transComp = CompBuilders
                .buildTransform(new Vector2(entry.x + entry.width, entry.y + entry.height), 0).get();
        transComp.z = 0.5f;

        EntityManager.build(entity, transComp, physicsCompBuilder.end(), stateComp, new EntryComponent(),
                new SensorComponent(),
                new InactiveComponent());

        return entity;
    }

    private Entity makeTreasure(UtilData treasure) {
        // TODO
        Shape shape = ShapeBuilder.circ(treasure.x + treasure.width, treasure.y + treasure.height, treasure.width);

        physicsCompBuilder.begin(BodyType.StaticBody, 0f, false);
        physicsCompBuilder.add("Sensor", shape, Globals.Categories.Sensor, Globals.Categories.Player, true);

        StateComponent stateComp = new StateComponent();
        stateComp.state = StateComponent.State.INACTIVE;

        Entity entity = new Entity();
        EntityManager.build(entity, physicsCompBuilder.end(), stateComp, new InactiveComponent());

        return entity;
    }

    private QuadData<Vector2> getSpawnPoints(QuadData<Vector2> spawnPoints) {
        QuadData<Vector2> points = spawnPoints.copy();
        points.map(point -> {
            if (point == null) {
                return null;
            }
            return point.cpy();
        });
        return points;
    }
}
