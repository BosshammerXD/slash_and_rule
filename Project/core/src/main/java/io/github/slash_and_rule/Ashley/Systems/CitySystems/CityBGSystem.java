package io.github.slash_and_rule.Ashley.Systems.CitySystems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Systems.InputSystem.MouseInputType;
import io.github.slash_and_rule.Utils.Mappers;

public class CityBGSystem extends EntitySystem {
    private SpriteBatch batch = new SpriteBatch();
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private OrthographicCamera gameCamera;
    private Texture grassTexture;
    private ImmutableArray<Entity> players;
    private Vector3 tempVector = new Vector3();
    private Vector3 lastMousePos = new Vector3();

    public CityBGSystem(OrthographicCamera gameCamera, int priority) {
        super(priority);
        this.gameCamera = gameCamera;
    }

    @Override
    public void addedToEngine(Engine engine) {
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class, ControllableComponent.class).get());
    }

    public void load(AssetManager assetManager) {
        this.grassTexture = assetManager.get("city/Grass.png", Texture.class);
        this.grassTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

    }

    @Override
    public void update(float deltaTime) {
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();

        // Berechne die tatsächliche Viewport-Größe unter Berücksichtigung des Zooms
        float viewportWidth = gameCamera.viewportWidth * gameCamera.zoom;
        float viewportHeight = gameCamera.viewportHeight * gameCamera.zoom;

        // Positioniere die Textur so, dass sie den sichtbaren Bereich abdeckt
        float x = gameCamera.position.x - viewportWidth / 2;
        float y = gameCamera.position.y - viewportHeight / 2;
        float width = viewportWidth;
        float height = viewportHeight;
        float cameraX = gameCamera.position.x / 2;
        float cameraY = gameCamera.position.y / 2;

        float uStart = (cameraX < 0) ? 1 + (cameraX % 1) : cameraX % 1;
        uStart += (gameCamera.zoom);
        float vStart = (cameraY < 0) ? -(cameraY % 1) : 1 - cameraY % 1;
        vStart += (gameCamera.zoom) * 2;

        float uEnd = uStart + 8f * gameCamera.zoom;
        float vEnd = vStart + 6f * gameCamera.zoom;

        // Zeichne die Textur mit korrekt berechneten UV-Koordinaten (V-Koordinaten
        // vertauscht um Flipping zu korrigieren)
        batch.draw(grassTexture, x, y, width, height,
                uStart, vEnd, uEnd, vStart);

        batch.end();

        for (Entity player : players) {
            ControllableComponent controllable = Mappers.controllableMapper.get(player);
            if (controllable == null) {
                continue;
            }
            for (ControllableComponent.MouseData data : controllable.mouseQueue) {

                if (data.type == MouseInputType.DOWN) {
                    tempVector.set(data.screenX, data.screenY, 0);
                    gameCamera.unproject(tempVector);
                    lastMousePos.set(tempVector);
                } else if (data.type == MouseInputType.DRAGGED) {
                    // Berechne die Differenz und bewege die Kamera
                    tempVector.set(data.screenX, data.screenY, 0);
                    gameCamera.unproject(tempVector);

                    xAcc += lastMousePos.x - tempVector.x;
                    yAcc += lastMousePos.y - tempVector.y;

                    if (Math.abs(xAcc) >= 1 / 32) {
                        gameCamera.position.add(((int) (xAcc * 32)) * 1 / 32f, 0, 0);
                        xAcc -= xAcc % (1 / 32f);
                    }
                    if (Math.abs(yAcc) >= 1 / 32) {
                        gameCamera.position.add(0, ((int) (yAcc * 32)) * 1 / 32f, 0);

                        yAcc -= yAcc % (1 / 32f);
                    }
                    gameCamera.update();

                    lastMousePos.set(tempVector);

                }
            }
            for (ControllableComponent.ScrollData data : controllable.scrollQueue) {

                // Verwende amountY für Zoom (vertikales Scrollen)
                float scrollAmount = data.amountY;
                float newZoom = gameCamera.zoom + scrollAmount * 0.1f;

                // Begrenze den Zoom zwischen 0.5f und 3.0f
                newZoom = Math.max(0.5f, Math.min(3.0f, newZoom));

                gameCamera.zoom = newZoom;
                gameCamera.update();
            }
            if (gameCamera.position.x < -16) {
                gameCamera.position.x = -16;
                xAcc = 0;
            }
            if (gameCamera.position.y < -8) {
                gameCamera.position.y = -8;
                yAcc = 0;
            }
            if (gameCamera.position.x > 16) {
                gameCamera.position.x = 16;
                xAcc = 0;
            }
            if (gameCamera.position.y > 8) {
                gameCamera.position.y = 8;
                yAcc = 0;
            }
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.setColor(1, 1, 1, 1);
        for (int i = -16; i <= 16; i++) {
            shapeRenderer.line(i, -8, i, 8); // Vertikale Linien
        }
        for (int i = -8; i <= 8; i++) {
            shapeRenderer.line(-16, i, 16, i); // Horizontale Linien
        }
        shapeRenderer.end();
    }

    private float xAcc = 0;
    private float yAcc = 0;
}
