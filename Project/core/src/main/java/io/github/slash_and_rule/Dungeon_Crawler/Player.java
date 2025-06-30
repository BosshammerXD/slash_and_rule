package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.Inputhandler;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Animations.MovementAnimData;
import io.github.slash_and_rule.Animations.triggeredAnimData;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.PlannedFixture;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.WeaponStates;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Systems.InputSystem.MouseInputType;
import io.github.slash_and_rule.Interfaces.Pausable;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.Mappers;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;

public class Player implements Pausable, Disposable {
    private EntityManager entityManager = new EntityManager();
    private Entity playerEntity;
    private static String[] animNames = { "MoveLeft", "MoveDown", "MoveRight", "MoveUp" };
    private static String[] capeAnimNames = { "CapeMoveLeft", "CapeMoveDown", "CapeMoveRight", "CapeMoveUp" };

    private TextureData moveTextureData;
    private TextureData capeTextureData;

    public Player(PhysicsBuilder physicsBuilder, OrthographicCamera camera, AtlasManager atlasManager) {
        CircleShape colliderShape = new CircleShape();
        colliderShape.setRadius(7 / 16f);

        atlasManager.add("entities/PlayerAtlas/PlayerAtlas.atlas");

        this.playerEntity = entityManager.reset();

        TransformComponent tC = new TransformComponent(
            new Vector2(2, 2), 0f
        );

        RenderableComponent rC = new RenderableComponent();
        MovementAnimData moveAnimData = new MovementAnimData(
            "entities/PlayerAtlas/PlayerAtlas.atlas", 
            () -> new Vector2().set(tC.position).sub(tC.lastPosition), 
            animNames, new float[] { 0.1f, 0.2f, 0.1f, 0.2f });
        this.moveTextureData = new TextureData() {
            {
                animData = moveAnimData;
                width = 2f;
                height = 2f;
                offsetX = -1f;
                offsetY = -0.5f;
            }
        };
        rC.addTextureDatas(1, this.moveTextureData);
        MovementAnimData capeMoveAnimData = new MovementAnimData(
            "entities/PlayerAtlas/PlayerAtlas.atlas",
            () -> new Vector2().set(tC.position).sub(tC.lastPosition),
            capeAnimNames, new float[] { 0.1f, 0.2f, 0.1f, 0.2f });
        
        this.capeTextureData = new TextureData() {
            {
                animData = capeMoveAnimData;
                width = 2f;
                height = 2f;
                offsetX = -1f;
                offsetY = -0.5f;
            }
        };
        rC.addTextureDatas(2, this.capeTextureData);

        ControllableComponent cC = new ControllableComponent(new PlayerInput(camera));

        PhysicsComponent pC = new PhysicsComponent();
        pC.body = physicsBuilder.makeBody(
                playerEntity,
                tC.position.x, tC.position.y,
                BodyType.DynamicBody, 7.5f, true);
        pC.body.setFixedRotation(true);
        pC.fixtures.put(
                "Collider",
                physicsBuilder.addFixture(
                        pC.body, colliderShape, 1f,
                        Globals.PlayerCategory, Globals.PlayerMask, false));

        PolygonShape weaponShape = new PolygonShape();
        weaponShape.setAsBox(0.5f, 0.25f, new Vector2(2, 0), 0);

        PlannedFixture[] fixtures = new PlannedFixture[] {
            new PlannedFixture(0f, 0.1f, weaponShape, Globals.PlayerCategory)
        };

        WeaponComponent wC = new WeaponComponent(
            physicsBuilder, playerEntity, fixtures, 
            10, 1f, 1f, 
            new TextureData() {
                {
                    animData = new triggeredAnimData("entities/PlayerAtlas/PlayerAtlas.atlas", "Attack", 0.1f, 0);
                    width = 4f;
                    height = 4f;
                    offsetX = -1f;
                    offsetY = -0.5f;
                }
            }
        );
        wC.body.setTransform(tC.position, 0);

        entityManager.build(new PlayerComponent(), new MidfieldComponent(), 
                rC, tC, cC, pC, wC,
                new MovementComponent(new Vector2(0f,0f), 10f),
                new HealthComponent());
        entityManager.finish();
    }

    private class PlayerInput extends Inputhandler {
        private OrthographicCamera camera;

        public PlayerInput(OrthographicCamera camera) {
            this.camera = camera;
        }

        @Override
        public void mouseEvent(MouseInputType type, int screenX, int screenY, int button) {
            if (type == MouseInputType.MOVED || type == MouseInputType.DRAGGED) {
                mouseMoved(screenX, screenY);
            } else if (type == MouseInputType.DOWN && button == Globals.AttackButton) {
                mousePressed();
            } else if (type == MouseInputType.UP && button == Globals.AttackButton) {
                mouseReleased();
            }
        }

        private void mouseMoved(int x, int y) {
            // Convert screen coordinates to world coordinates
            Vector3 worldCoords = camera.unproject(new Vector3(x, y, 0));
            apply(Mappers.weaponMapper, comp -> {
                Vector2 weaponPos = comp.body.getPosition();
                comp.target = new Vector2(worldCoords.x - weaponPos.x, worldCoords.y - weaponPos.y);
            });
        }

        private void mousePressed() {
            apply(Mappers.weaponMapper, comp -> {
                if (comp.state != WeaponStates.IDLE) {
                    return;
                }
                if (comp.chargetime != 0f) {
                    comp.time = 0f;
                    comp.state = WeaponStates.CHARGING;
                } else {
                    comp.state = WeaponStates.ATTACKING;
                }
            });
        }

        private void mouseReleased() {
            apply(Mappers.weaponMapper, comp -> {
                if (comp.state == WeaponStates.CHARGING) {
                    comp.state = WeaponStates.ATTACKING;
                }
            });
        }

        @Override
        public void pollevent() {
            movement();
            apply(Mappers.transformMapper, comp -> {
                camera.position.set(comp.position.x, comp.position.y, 0);
            });
            apply(Mappers.renderableMapper, this::animation);
        }

        private void movement() {
            Vector2 velocity = new Vector2(0, 0);
            if (Gdx.input.isKeyPressed(Globals.MoveUpKey)) {
                velocity.y += 1;
            }
            if (Gdx.input.isKeyPressed(Globals.MoveDownKey)) {
                velocity.y -= 1;
            }
            if (Gdx.input.isKeyPressed(Globals.MoveLeftKey)) {
                velocity.x -= 1;
            }
            if (Gdx.input.isKeyPressed(Globals.MoveRightKey)) {
                velocity.x += 1;
            }
            velocity.nor(); // Normalize the velocity vector
            apply(Mappers.movementMapper, comp -> {
                comp.velocity.set(velocity).scl(comp.max_speed);
            });
        }

        private void animation(RenderableComponent comp) {
            if (moveTextureData.animData.name.equals("MoveUp")) {
                comp.changePriority(0, 2, capeTextureData);
            } else {
                comp.changePriority(2, 0, capeTextureData);
            }
        }
    }

    @Override
    public void pause() {
        // Implement pause logic for the player
    }

    @Override
    public void resume() {
        // Implement resume logic for the player
    }

    @Override
    public void dispose() {
        // TODO: Figure out, what we need to dispose here
    }
}
