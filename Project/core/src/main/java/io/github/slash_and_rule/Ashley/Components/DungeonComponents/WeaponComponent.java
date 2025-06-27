package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

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

        public ProjectileData(Body body, Fixture fixture, Vector2 velocity, float lifetime, int damage, float weight) {
            this.body = body;
            this.fixture = fixture;
            this.velocity = velocity;
            this.lifetime = lifetime;
            this.damage = damage;
            this.weight = weight;
        }
    }

    public class plannedFixture {
        public float start;
        public float end;
        public Fixture fixture;

        public plannedFixture(float start, float end, Fixture fixture) {
            this.start = start;
            this.end = end;
            this.fixture = fixture;
        }
    }

    public Body body;
    // Die hitboxen mit den Zeiten wann sie aktiv sind (während der Animation)
    public plannedFixture[] fixtures;

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
    public float chargetime = 0f;

    // Die Projektile die die Waffe schießt
    public ProjectileData[] projectiles;
}
