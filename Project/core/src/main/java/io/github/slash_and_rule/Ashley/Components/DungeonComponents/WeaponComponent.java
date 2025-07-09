package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;

import io.github.slash_and_rule.Animations.triggeredAnimData;
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

    // Die Textur / Animations Daten der Waffe
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
        this.animData = null;
        this.damage = 0;
        this.weight = 0f;
        this.cooldown = 0f;
        this.time = 0f;
        this.chargetime = 0f;
        this.projectiles = new ProjectileData[0];
    }
}
