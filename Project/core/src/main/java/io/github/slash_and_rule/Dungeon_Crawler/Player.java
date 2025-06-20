package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Interfaces.SortedDisplayable;
import io.github.slash_and_rule.Interfaces.Updatetable;
import io.github.slash_and_rule.Utils.ColliderObject;
import io.github.slash_and_rule.Interfaces.Pausable;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;

public class Player implements SortedDisplayable, Updatetable, Pausable, Disposable {
    private float max_speed = 10f; // Maximum speed of the player

    private PhysicsScreen screen;

    private ColliderObject hitbox;

    private Entity playerEntity;

    public Player(PhysicsScreen screen, InputManager inputManager) {
        CircleShape hitboxShape = new CircleShape();
        hitboxShape.setRadius(7 / 16f); // Set the radius of the player's hitbox
        this.hitbox = new ColliderObject(screen, 1f, 7.5f, 0f, 2.5f, 2.5f, Globals.PlayerCategory, Globals.PlayerMask,
                hitboxShape, BodyType.DynamicBody, true);

        this.screen = screen;

        screen.getAtlasManager().add("entities/PlayerAtlas/PLayerAtlas.atlas");

        EntityManager.start();

        TransformComponent Tc = new TransformComponent();
        Tc.pos = new Vector2(2, 2); // Initial position of the player
        Tc.rotation = 0f; // Initial rotation of the player

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
                offsetX = 0f;
                offsetY = 0f;
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
                offsetX = 0f;
                offsetY = 0f;
            }
        };
        RenderableComponent rC = new RenderableComponent();
        rC.textures = new TextureData[] {
                moveData,
                capeMoveData
        };

        EntityManager.add(new PlayerComponent(), new MidfieldComponent(), rC, Tc);

        EntityManager.end();
    }

    private Vector2 lastPos = new Vector2(0, 0);

    private void processAnim(RenderableComponent.AnimData animData, float delta) {
        animData.stateTime += delta;
        while (animData.stateTime >= 0.5f) {
            animData.stateTime -= 0.5f;
            animData.animIndex++;
        }
    }

    public Entity getPlayerEntity() {
        return playerEntity;
    }

    @Override
    public void update(float delta) {
        // Implement update logic for the player

        Body body = hitbox.getBody();

        Vector2 pos = body.getPosition();

        Vector2 movDir = new Vector2(0, 0);

        if (Gdx.input.isKeyPressed(Keys.W)) {
            movDir.y += 1;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) {
            movDir.y -= 1;
        }
        if (Gdx.input.isKeyPressed(Keys.A)) {
            movDir.x -= 1;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) {
            movDir.x += 1;
        }

        movDir.nor();

        movDir.scl(this.max_speed);

        body.setLinearVelocity(movDir);

        Vector2 moveVec = new Vector2(pos.x - lastPos.x, pos.y - lastPos.y);

        lastPos.set(pos);

        screen.camera.position.set(pos.x, pos.y, 0);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Implement drawing logic for the player
        Vector2 pos = hitbox.getBody().getPosition();
        float x = pos.x - 1f;
        float y = pos.y - 0.5f;
    }

    @Override
    public void pause() {
        // Implement pause logic for the player
    }

    @Override
    public void resume() {
        // Implement resume logic for the player
    }

    public void setPosition(float x, float y) {
        hitbox.getBody().setTransform(x, y, 0); // Set the player's position in the world
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public float getSortIndex() {
        return this.hitbox.getBody().getPosition().y; // Use the y position for sorting
    }
}
