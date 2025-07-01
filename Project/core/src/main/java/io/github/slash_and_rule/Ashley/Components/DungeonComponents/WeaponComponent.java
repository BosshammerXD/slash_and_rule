package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import java.util.ArrayDeque;
import java.util.TreeMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Animations.triggeredAnimData;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

public class WeaponComponent implements Component {
    public static class ProjectileData {
        public Body body;
        public Fixture fixture;
        public Vector2 velocity;
        public float lifetime;
        public int damage;
        public float weight;
        public float drag = 0f;
        public TextureData textureData;

        public ProjectileData(Body body, Fixture fixture, Vector2 velocity, float lifetime, int damage, float weight,
                TextureData textureData) {
            this.body = body;
            this.fixture = fixture;
            this.velocity = velocity;
            this.lifetime = lifetime;
            this.damage = damage;
            this.weight = weight;
            this.textureData = textureData;
        }
    }

    public static class PlannedFixture {
        public float start;
        public float end;
        public Shape shape;

        public PlannedFixture(float start, float end, Shape shape, short maskBits) {
            this.start = start;
            this.end = end;
            this.shape = shape;
        }
    }

    public static class timedActions implements Runnable {
        public float time;
        private Runnable[] actions;

        public timedActions(float time, Runnable[] actions) {
            this.time = time;
            this.actions = actions;
        }

        @Override
        public void run() {
            for (Runnable action : actions) {
                action.run();
            }
        }
    }

    public static enum WeaponStates {
        IDLE, ATTACKING, CHARGING, COOLDOWN
    }

    public WeaponStates state = WeaponStates.IDLE;

    // Inputflags
    public Vector2 target = new Vector2(1, 0);

    public Body body;
    public RevoluteJoint joint;
    // Die hitboxen mit den Zeiten wann sie aktiv sind (während der Animation)
    public int index = 0;
    public timedActions[] fixtures;

    public void buildFixtures(PhysicsBuilder physicsBuilder, PlannedFixture... fixtures) {
        TreeMap<Float, ArrayDeque<Runnable>> fixtureMap = new TreeMap<>();
        for (PlannedFixture fixtureData : fixtures) {
            Fixture fixture = physicsBuilder.addFixture(this.body, fixtureData.shape, 1f, Globals.HitboxCategory,
                    (short) 0, true);

            applyCategory(fixtureMap, fixture, fixtureData.start, Globals.HitboxCategory, physicsBuilder);
            applyCategory(fixtureMap, fixture, fixtureData.end, (short) 0, physicsBuilder);
        }
        this.fixtures = new timedActions[fixtureMap.size()];
        int index = 0;
        for (Float time : fixtureMap.keySet()) {
            ArrayDeque<Runnable> actionQueue = fixtureMap.get(time);
            Runnable[] actions = actionQueue.toArray(new Runnable[0]);
            this.fixtures[index++] = new timedActions(time, actions);
        }
    }

    private void applyCategory(TreeMap<Float, ArrayDeque<Runnable>> fixtureMap, Fixture fixture,
            float time, short categoryBits, PhysicsBuilder physicsBuilder) {
        ArrayDeque<Runnable> actionQueue = getDeque(fixtureMap, time);
        actionQueue.add(() -> {
            Filter filter = fixture.getFilterData();
            filter.categoryBits = categoryBits;
            fixture.setFilterData(filter);
        });
    }

    private ArrayDeque<Runnable> getDeque(TreeMap<Float, ArrayDeque<Runnable>> fixtureMap, float time) {
        ArrayDeque<Runnable> actionQueue = fixtureMap.get(time);
        if (actionQueue == null) {
            actionQueue = new ArrayDeque<>();
            fixtureMap.put(time, actionQueue);
        }
        return actionQueue;
    }

    // Die Textur / Animations Daten der Waffe
    public WeaponTextureData textureData;
    public TextureData texture;
    public triggeredAnimData animData;

    // Werte für Schaden und Rückstoß
    public int damage;
    public float weight;

    // Wie lange die Waffe braucht um nach dem letzten Angriff wieder einsatzbereit
    // zu sein
    public float cooldown;
    public float time = 0f;

    // Wie lange bis die Waffe vollständig aufgeladen ist (bspw. das ziehen von
    // einem Bogen)
    public float chargeVal = 0f;
    public float chargetime = 0f;

    // Die Projektile die die Waffe schießt
    public ProjectileData[] projectiles;

    public WeaponComponent() {
        this.body = null;
        this.joint = null;
        this.textureData = new WeaponTextureData();
        this.animData = new triggeredAnimData("", "", 0.1f, 0);
        this.damage = 0;
        this.weight = 0f;
        this.cooldown = 0f;
        this.time = 0f;
        this.chargetime = 0f;
        this.projectiles = new ProjectileData[0];
    }

    public WeaponComponent(PhysicsBuilder physicsBuilder, Entity entity, PlannedFixture[] fixtures, int damage,
            float weight, float cooldown,
            WeaponTextureData textureData, ProjectileData[] projectiles) {
        this.body = physicsBuilder.makeBody(entity, BodyType.DynamicBody, 0, true);
        this.buildFixtures(physicsBuilder, fixtures);
        this.damage = damage;
        this.weight = weight;
        this.cooldown = cooldown;
        this.projectiles = projectiles;
        this.textureData = textureData;
        MassData mD = this.body.getMassData();
        mD.mass = 0.001f;
        this.body.setMassData(mD);
    }

    public WeaponComponent(PhysicsBuilder physicsBuilder, Entity entity, PlannedFixture[] fixtures, int damage,
            float weight, float cooldown,
            WeaponTextureData textureData) {
        this(physicsBuilder, entity, fixtures, damage, weight, cooldown, textureData, new ProjectileData[0]);
    }

    public static class WeaponTextureData {
        public String atlasPath;
        public String animName;
        public float frameDuration;
        public int priority;
        public float width;
        public float height;
        public float offsetX;
        public float offsetY;

        public WeaponTextureData(String atlasPath, String animName, float frameDuration, int priority, float width,
                float height, float offsetX, float offsetY) {
            this.atlasPath = atlasPath;
            this.animName = animName;
            this.frameDuration = frameDuration;
            this.priority = priority;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public WeaponTextureData() {
            this.atlasPath = "";
            this.animName = "";
            this.frameDuration = 0.1f;
            this.priority = 0;
            this.width = 1f;
            this.height = 1f;
            this.offsetX = 0f;
            this.offsetY = 0f;
        }
    }
}
