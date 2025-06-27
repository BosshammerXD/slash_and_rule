package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.Inputhandler;
import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Animations.AnimationFunctions;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Interfaces.Pausable;
import io.github.slash_and_rule.Utils.Mappers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;

public class Player implements Pausable, Disposable {
    private EntityManager entityManager = new EntityManager();
    private Entity playerEntity;
    private static String[] animNames = { "MoveLeft", "MoveDown", "MoveRight", "MoveUp" };
    private static String[] capeAnimNames = { "CapeMoveLeft", "CapeMoveDown", "CapeMoveRight", "CapeMoveUp" };

    private TextureData moveTextureData;
    private TextureData capeTextureData;

    public Player(PhysicsScreen screen) {
        CircleShape colliderShape = new CircleShape();
        colliderShape.setRadius(7 / 16f);

        screen.getAtlasManager().add("entities/PlayerAtlas/PlayerAtlas.atlas");

        this.playerEntity = entityManager.reset();

        TransformComponent tC = new TransformComponent();
        tC.position = new Vector2(2, 2); // Initial position of the player
        tC.rotation = 0f; // Initial rotation of the player

        RenderableComponent rC = new RenderableComponent();
        RenderableComponent.AnimData moveAnimData = new RenderableComponent.AnimData(
                "entities/PlayerAtlas/PlayerAtlas.atlas",
                "MoveDown",
                AnimationFunctions.mappedTimes(
                        AnimationFunctions.makeNameTimes(animNames, new float[] { 0.1f, 0.2f, 0.1f, 0.2f }), 0));
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
        RenderableComponent.AnimData capeMoveAnimData = new RenderableComponent.AnimData(
                "entities/PlayerAtlas/PlayerAtlas.atlas",
                "CapeMoveDown",
                AnimationFunctions.mappedTimes(
                        AnimationFunctions.makeNameTimes(capeAnimNames, new float[] { 0.1f, 0.2f, 0.1f, 0.2f }), 0));
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
        MovementComponent mC = new MovementComponent();
        mC.max_speed = 10f;
        mC.velocity = new Vector2(0, 0);

        ControllableComponent cC = new ControllableComponent();
        cC.inputhandler = new PlayerInput(screen.camera);

        PhysicsComponent pC = new PhysicsComponent();
        pC.body = screen.getPhysicsBuilder().makeBody(
                playerEntity,
                tC.position.x, tC.position.y,
                BodyType.DynamicBody, 7.5f, true);

        pC.fixtures.put(
                "Collider",
                screen.getPhysicsBuilder().addFixture(
                        pC.body, colliderShape, 1f,
                        Globals.PlayerCategory, Globals.PlayerMask, false));

        entityManager.build(new PlayerComponent(), new MidfieldComponent(), rC, tC, mC, cC, pC);
        entityManager.finish();
    }

    private class PlayerInput extends Inputhandler {
        private OrthographicCamera camera;
        private TransformComponent tC;

        public PlayerInput(OrthographicCamera camera) {
            this.camera = camera;
        }

        @Override
        public void preEvents(Entity entity) {
            tC = Mappers.transformMapper.get(entity);
        }

        @Override
        public void pollevent() {
            movement();
            camera.position.set(tC.position.x, tC.position.y, 0);
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
            if (tC == null) {
                return;
            }
            Vector2 moveVec = new Vector2(
                    tC.position.x - tC.lastPosition.x,
                    tC.position.y - tC.lastPosition.y);

            AnimationFunctions.movementAnimData(
                    moveTextureData.animData, moveVec,
                    animNames,
                    delta, 1f);
            if (moveTextureData.animData.name.equals("MoveUp")) {
                comp.changePriority(0, 2, capeTextureData);
            } else {
                comp.changePriority(2, 0, capeTextureData);
            }
            AnimationFunctions.movementAnimData(
                    capeTextureData.animData, moveVec,
                    capeAnimNames,
                    delta, 1f);
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
        // TODO Auto-generated method stub

    }
}
