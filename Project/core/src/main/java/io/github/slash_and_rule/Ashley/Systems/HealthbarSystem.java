package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class HealthbarSystem extends IteratingSystem {
    private ShapeRenderer renderer;
    private OrthographicCamera camera;

    public HealthbarSystem(OrthographicCamera camera, int priority) {
        super(Family.all(HealthComponent.class, TransformComponent.class).exclude(PlayerComponent.class).get(),
                priority);
        this.renderer = new ShapeRenderer();
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HealthComponent health = Mappers.healthMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        float healthPercentage = (float) health.health / health.maxHealth;
        float barWidth = 1f; // Width of the health bar
        float barHeight = 0.1f; // Height of the health bar
        float x = transform.position.x - barWidth / 2;
        float y = transform.position.y + health.offsetY; // Offset for the health bar
        renderer.setColor(0, 0, 0, 1);
        renderer.rect(x, y, barWidth, barHeight); // Draw background
        renderer.setColor(1, 0, 0, 1); // Red color for the health bar
        renderer.rect(x, y, barWidth * healthPercentage, barHeight);
        renderer.end();
    }
}
