package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.InputManager;
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
import io.github.slash_and_rule.Interfaces.Inputhandler;
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
    private Entity playerEntity;
    private static String[] animNames = { "MoveLeft", "MoveDown", "MoveRight", "MoveUp" };
    private static String[] capeAnimNames = { "CapeMoveLeft", "CapeMoveDown", "CapeMoveRight", "CapeMoveUp" };

    public Player(PhysicsScreen screen, InputManager inputManager) {
        CircleShape colliderShape = new CircleShape();
        colliderShape.setRadius(7 / 16f);

        screen.getAtlasManager().add("entities/PlayerAtlas/PLayerAtlas.atlas");

        TransformComponent tC = new TransformComponent();
        tC.position = new Vector2(2, 2); // Initial position of the player
        tC.rotation = 0f; // Initial rotation of the player

        RenderableComponent.AnimData moveAnimData = new RenderableComponent.AnimData(
                "entities/PlayerAtlas/PLayerAtlas.atlas",
                "MoveDown",
                AnimationFunctions.mappedTimes(
                        AnimationFunctions.makeNameTimes(animNames, new float[] { 0.1f, 0.2f, 0.1f, 0.2f }), 0));
        TextureData moveData = new TextureData() {
            {
                animData = moveAnimData;
                priority = 1;
                width = 2f;
                height = 2f;
                offsetX = -1f;
                offsetY = -0.5f;
            }
        };
        RenderableComponent.AnimData capeMoveAnimData = new RenderableComponent.AnimData(
                "entities/PlayerAtlas/PLayerAtlas.atlas",
                "CapeMoveDown",
                AnimationFunctions.mappedTimes(
                        AnimationFunctions.makeNameTimes(capeAnimNames, new float[] { 0.1f, 0.2f, 0.1f, 0.2f }), 0));
        TextureData capeMoveData = new TextureData() {
            {
                animData = capeMoveAnimData;
                priority = 2;
                width = 2f;
                height = 2f;
                offsetX = -1f;
                offsetY = -0.5f;
            }
        };
        RenderableComponent rC = new RenderableComponent();
        rC.textures = new TextureData[] {
                moveData,
                capeMoveData
        };
        MovementComponent mC = new MovementComponent();
        mC.max_speed = 5f;
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

        this.playerEntity = EntityManager.makeEntity(new PlayerComponent(), new MidfieldComponent(), rC, tC, mC, cC,
                pC);
    }

    private static class PlayerInput implements Inputhandler {
        private OrthographicCamera camera;

        public PlayerInput(OrthographicCamera camera) {
            this.camera = camera;
        }

        @Override
        public void handleInput(Entity entity, float deltaTime) {
            MovementComponent movement = Mappers.movementMapper.get(entity);
            RenderableComponent render = Mappers.renderableMapper.get(entity);
            if (movement == null || render == null) {
                return;
            }
            movement.velocity.set(0, 0); // Reset velocity each frame
            if (Gdx.input.isKeyPressed(Globals.MoveUpKey)) {
                movement.velocity.y += 1;
            }
            if (Gdx.input.isKeyPressed(Globals.MoveDownKey)) {
                movement.velocity.y -= 1;
            }
            if (Gdx.input.isKeyPressed(Globals.MoveLeftKey)) {
                movement.velocity.x -= 1;
            }
            if (Gdx.input.isKeyPressed(Globals.MoveRightKey)) {
                movement.velocity.x += 1;
            }
            movement.velocity.nor(); // Normalize the velocity vector
            movement.velocity.scl(movement.max_speed); // Scale by max speed

            TransformComponent transform = Mappers.transformMapper.get(entity);
            if (transform == null) {
                return;
            }
            Vector2 pos = transform.position;

            camera.position.set(pos.x, pos.y, 0);

            Vector2 moveVec = new Vector2(
                    pos.x - transform.lastPosition.x,
                    pos.y - transform.lastPosition.y);

            AnimationFunctions.movementAnimData(
                    render.textures[0].animData, moveVec,
                    animNames,
                    deltaTime, 1f);
            if (render.textures[0].animData.name.equals("MoveUp")) {
                render.textures[1].priority = 2;
            } else {
                render.textures[1].priority = 0;
            }
            AnimationFunctions.movementAnimData(
                    render.textures[1].animData, moveVec,
                    capeAnimNames,
                    deltaTime, 1f);
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
