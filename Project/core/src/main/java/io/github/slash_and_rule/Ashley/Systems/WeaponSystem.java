package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;

public class WeaponSystem extends IteratingSystem {
    public WeaponSystem(int priority) {
        super(Family.all(WeaponComponent.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Hier wird die Logik für die Waffenverarbeitung implementiert
        // Zum Beispiel: Überprüfen, ob die Waffe bereit ist, Schaden zu verursachen
        // und dann die entsprechenden Aktionen ausführen.
    }
}
