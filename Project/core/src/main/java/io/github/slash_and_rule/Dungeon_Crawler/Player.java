package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.InputManager;
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

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;

public class Player implements Pausable, Disposable {
    private Entity playerEntity;

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
                this::processAnim);
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
                this::processAnim);
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
                        Globals.PlayerCategory, Globals.PlayerMask));

        this.playerEntity = EntityManager.makeEntity(new PlayerComponent(), new MidfieldComponent(), rC, tC, mC, cC,
                pC);
    }

    private void processAnim(RenderableComponent.AnimData animData, float delta) {
        animData.stateTime += delta;
        while (animData.stateTime >= 0.1f) {
            animData.stateTime -= 0.5f;
            animData.animIndex++;
        }
    }

    private static class PlayerInput implements Inputhandler {
        public Vector2 lastpos = new Vector2(0, 0);

        private OrthographicCamera camera;

        public PlayerInput(OrthographicCamera camera) {
            this.camera = camera;
        }

        private ComponentMapper<TransformComponent> transformMapper = ComponentMapper
                .getFor(TransformComponent.class);
        private ComponentMapper<MovementComponent> movementMapper = ComponentMapper
                .getFor(MovementComponent.class);

        @Override
        public void handleInput(Entity entity, float deltaTime) {
            MovementComponent movement = movementMapper.get(entity);
            if (movement == null) {
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

            TransformComponent transform = transformMapper.get(entity);
            if (transform == null) {
                return;
            }
            Vector2 pos = transform.position;

            camera.position.set(pos.x, pos.y, 0);
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
