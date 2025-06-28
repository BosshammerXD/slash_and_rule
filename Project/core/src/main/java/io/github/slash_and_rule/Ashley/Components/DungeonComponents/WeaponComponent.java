package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import java.util.ArrayDeque;
import java.util.TreeMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;

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
        public Fixture fixture;

        public PlannedFixture(float start, float end, Fixture fixture) {
            this.start = start;
            this.end = end;
            this.fixture = fixture;
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

    public void buildFixtures(PlannedFixture... fixtures) {
        TreeMap<Float, ArrayDeque<Runnable>> fixtureMap = new TreeMap<>();
        for (PlannedFixture fixture : fixtures) {
            applyCategory(fixtureMap, fixture, Globals.HitboxCategory);
            applyCategory(fixtureMap, fixture, (short) 0);
        }
        this.fixtures = new timedActions[fixtureMap.size()];
        int index = 0;
        for (Float time : fixtureMap.keySet()) {
            ArrayDeque<Runnable> actionQueue = fixtureMap.get(time);
            Runnable[] actions = actionQueue.toArray(new Runnable[0]);
            this.fixtures[index++] = new timedActions(time, actions);
        }
    }

    private void applyCategory(TreeMap<Float, ArrayDeque<Runnable>> fixtureMap, PlannedFixture fixtureData,
            short categoryBits) {
        float time = categoryBits == 0 ? fixtureData.end : fixtureData.start;
        ArrayDeque<Runnable> actionQueue = getDeque(fixtureMap, time);
        Fixture fixture = fixtureData.fixture;
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
    public TextureData textureData;

    // Werte für Schaden und Rückstoß
    public int damage;
    public float weight;

    // Wie lange die Waffe braucht um nach dem letzten Angriff wieder einsatzbereit
    // zu sein
    public float cooldown;
    public float time;

    // Wie lange bis die Waffe vollständig aufgeladen ist (bspw. das ziehen von
    // einem Bogen)
    public float chargeVal = 0f;
    public float chargetime = 0f;

    // Die Projektile die die Waffe schießt
    public ProjectileData[] projectiles;
}
