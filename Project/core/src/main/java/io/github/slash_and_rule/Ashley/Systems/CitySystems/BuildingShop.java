package io.github.slash_and_rule.Ashley.Systems.CitySystems;

import java.util.Arrays;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Signals;
import io.github.slash_and_rule.Ashley.Builder.CompBuilders;
import io.github.slash_and_rule.Ashley.Builder.RenderBuilder;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.CityComponents.BuildingComponent;
import io.github.slash_and_rule.Ashley.Components.CityComponents.BuyableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.ForegroundComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Ashley.Systems.InputSystem.MouseInputType;
import io.github.slash_and_rule.CityBuilder.CityData;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.Mappers;

public class BuildingShop extends EntitySystem {
    private Camera uiCamera;
    private Camera gameCamera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private AtlasManager atlasManager;

    private ImmutableArray<Entity> buildings;
    private ImmutableArray<Entity> input;
    private int numBuildings = 0;

    public BuildingShop(Camera camera, Camera gCamera, AtlasManager atlasManager, int priority) {
        super(priority);
        this.uiCamera = camera;
        this.gameCamera = gCamera;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.atlasManager = atlasManager;
    }

    @Override
    public void addedToEngine(Engine engine) {
        buildings = engine.getEntitiesFor(
                Family.all(BuildingComponent.class, BuyableComponent.class, ForegroundComponent.class).get());
        input = engine.getEntitiesFor(Family.all(ControllableComponent.class, PlayerComponent.class).get());

        engine.addEntityListener(Family.all(BuildingComponent.class, ForegroundComponent.class).get(),
                new EntityListener() {
                    @Override
                    public void entityAdded(Entity entity) {
                        numBuildings++;
                    }

                    @Override
                    public void entityRemoved(Entity entity) {
                        numBuildings--;
                    }
                });
    }

    @Override
    public void update(float deltaTime) {
        float x = uiCamera.position.x - uiCamera.viewportWidth / 2;
        float y = uiCamera.position.y - uiCamera.viewportHeight / 2;

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.41f, 0.355f, 0.275f, 1f);
        shapeRenderer.rect(x, y, uiCamera.viewportWidth, 64);
        shapeRenderer.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        x = -numBuildings * 32 / 2;
        for (Entity building : buildings) {
            ForegroundComponent renderableComponent = Mappers.foregroundMapper.get(building);

            drawBuildingUI(renderableComponent, x, y);
            x += 64; // Move to the next building position
        }
        drawBuildingGame();
        batch.end();

        for (Entity entity : input) {
            ControllableComponent controllable = Mappers.controllableMapper.get(entity);
            handleInput(controllable);
        }
    }

    private void drawBuildingUI(ForegroundComponent renderComp, float x, float y) {
        if (renderComp.dirty) {
            Arrays.sort(renderComp.textures);
            renderComp.dirty = false;
        }

        for (TextureData data : renderComp.textures) {
            if (data.texture == null) {
                if (data.atlasPath == null || data.name == null) {
                    continue; // Skip if no texture data is available
                }
                data.texture = atlasManager.getTexture(data.atlasPath, data.name);
            }

            TextureRegion region = data.texture;

            float width = 64;
            float height = 64;

            x = x + (data.offsetX * 32) - (width / 2);
            y = y + (data.offsetY * 32);

            batch.draw(region, x, y, width, height);
        }
    }

    private void drawBuildingGame() {
        Entity heldEntity = CityData.heldEntity;
        if (heldEntity == null) {
            return; // No building is currently held
        }
        ForegroundComponent renderComp = Mappers.foregroundMapper.get(heldEntity);
        TransformComponent transform = Mappers.transformMapper.get(heldEntity);

        if (renderComp == null || transform == null) {
            System.out.println("No ForegroundComponent or TransformComponent found for the held entity.");
            return;
        }

        if (renderComp.dirty) {
            Arrays.sort(renderComp.textures);
            renderComp.dirty = false;
        }
        batch.setProjectionMatrix(gameCamera.combined);
        for (TextureData data : renderComp.textures) {
            if (data.texture == null) {
                if (data.atlasPath == null || data.name == null) {
                    continue; // Skip if no texture data is available
                }
                data.texture = atlasManager.getTexture(data.atlasPath, data.name);
            }

            TextureRegion region = data.texture;

            batch.draw(region, transform.position.x,
                    transform.position.y, data.width, data.height);
        }
    }

    private void handleInput(ControllableComponent controllable) {
        for (ControllableComponent.MouseData data : controllable.mouseQueue) {
            if (data.type == MouseInputType.DOWN) {
                Vector3 tempVector = new Vector3(data.screenX, data.screenY, 0);
                uiCamera.unproject(tempVector);

                int index = isMouseOverBuilding(tempVector);
                if (index != -1) {
                    changeHeldBuilding(index, data.screenX, data.screenY);
                } else if (CityData.heldEntity != null) {
                    Signals.placeBuildingSignal.dispatch(new Signals.PlaceBuildingEvent(CityData.heldEntity));
                }
            }
            if (data.type == MouseInputType.MOVED) {
                Vector3 tempVector = new Vector3(data.screenX, data.screenY, 0);
                gameCamera.unproject(tempVector);
                moveBuilding(tempVector);
            }
        }
    }

    private int isMouseOverBuilding(Vector3 mouse) {
        float width = 64; // Width of each building
        float height = 64; // Height of each building
        float buildingY = uiCamera.position.y - uiCamera.viewportHeight / 2;

        for (int buildingIndex = 0; buildingIndex < buildings.size(); buildingIndex++) {
            float buildingX = -numBuildings * 32 / 2 + buildingIndex * 64 - 32;

            boolean check = mouse.x >= buildingX && mouse.x <= buildingX + width &&
                    mouse.y >= buildingY && mouse.y <= buildingY + height;

            if (check) {
                return buildingIndex;
            }
        }

        return -1;
    }

    private void moveBuilding(Vector3 mouse) {
        if (CityData.heldEntity == null) {
            return;
        }
        TransformComponent transform = Mappers.transformMapper.get(CityData.heldEntity);
        if (transform != null) {
            if (mouse.x < -16) {
                mouse.x = -16;
            } else if (mouse.x > 15) {
                mouse.x = 15;
            }
            if (mouse.y < -8) {
                mouse.y = -8;
            } else if (mouse.y > 7) {
                mouse.y = 7;
            }
            transform.position.set((int) mouse.x, (int) mouse.y);
        } else {
            System.out.println("No TransformComponent found for the held entity.");
        }
    }

    private RenderBuilder<ForegroundComponent> renderBuilder = new RenderBuilder<>();

    private void changeHeldBuilding(int index, int x, int y) {
        if (index < 0 || index >= buildings.size()) {
            System.out.println("Index out of bounds: " + index);
            return;
        }

        Vector3 tempVec = new Vector3(x, y, 0);
        gameCamera.unproject(tempVec);

        Entity building = buildings.get(index);
        ForegroundComponent renderComp = Mappers.foregroundMapper.get(building);

        CityData.heldEntity = new Entity();

        TransformComponent transform = CompBuilders.buildTransform(new Vector2(tempVec.x, tempVec.y), 0f).get();
        BuildingComponent buildComp = Mappers.buildingMapper.get(building).copy();

        renderBuilder.begin(new ForegroundComponent());
        for (TextureData data : renderComp.textures) {
            TextureData copy = data.copy();
            renderBuilder.add(copy);
        }
        renderBuilder.end(CityData.heldEntity);
        EntityManager.build(CityData.heldEntity, transform, buildComp, new BuyableComponent());
    }
}
