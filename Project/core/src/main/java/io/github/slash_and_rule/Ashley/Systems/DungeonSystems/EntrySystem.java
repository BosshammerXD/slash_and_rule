package io.github.slash_and_rule.Ashley.Systems.DungeonSystems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.EntryComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class EntrySystem extends IteratingSystem {
    private SpriteBatch batch = new SpriteBatch();
    private OrthographicCamera gCamera;
    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private BitmapFont font = new BitmapFont();
    private GlyphLayout layout = new GlyphLayout();
    private float halfTextWidth;
    private Runnable loader;

    public EntrySystem(OrthographicCamera camera, ExtendViewport viewport, OrthographicCamera gCamera, int priority,
            Runnable loader) {
        super(Family.all(EntryComponent.class, SensorComponent.class, TransformComponent.class)
                .exclude(InactiveComponent.class).get(), priority);
        this.camera = camera;
        this.viewport = viewport;
        this.gCamera = gCamera;

        layout.setText(font, "Press E to Exit");
        halfTextWidth = layout.width / 2;
        font.setColor(Color.WHITE);
        this.loader = loader;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SensorComponent sensor = Mappers.sensorMapper.get(entity);
        TransformComponent transform = Mappers.transformMapper.get(entity);
        EntryComponent entry = Mappers.entryMapper.get(entity);

        if (!sensor.contactsStarted.isEmpty()) {
            entry.isActive = true;
        }
        if (!sensor.contactsEnded.isEmpty()) {
            entry.isActive = false;
        }
        if (entry.isActive) {
            // Try without changing camera position first
            camera.position.set(gCamera.position.x, gCamera.position.y, 0);
            camera.zoom = gCamera.zoom;
            viewport.apply();
            camera.update();

            float textX = (transform.position.x - camera.position.x) * 32 - halfTextWidth;
            float textY = (transform.position.y - camera.position.y + 1.2f) * 32;

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            font.draw(batch, "Press E to Exit", textX, textY);

            batch.end();

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.E)) {
                entry.isActive = false;
                if (loader != null) {
                    loader.run();
                }
            }
        }
    }
}
