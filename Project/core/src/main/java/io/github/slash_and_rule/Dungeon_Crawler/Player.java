package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.InputManager;

import io.github.slash_and_rule.Interfaces.Displayable;
import io.github.slash_and_rule.Interfaces.Updatetable;
import io.github.slash_and_rule.Utils.ColliderObject;
import io.github.slash_and_rule.Interfaces.Pausable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Player implements Displayable, Updatetable, Pausable {
    private boolean isPaused = false;
    private float max_speed = 10f; // Maximum speed of the player

    private PhysicsScreen screen;

    private ColliderObject hitbox;

    public Player(PhysicsScreen screen, InputManager inputManager) {
        CircleShape hitboxShape = new CircleShape();
        hitboxShape.setRadius(0.5f); // Set the radius of the player's hitbox
        this.hitbox = new ColliderObject(screen, 1f, 7.5f, 0f, 2.5f, 2.5f, Globals.PlayerCategory, Globals.PlayerMask,
                hitboxShape, BodyType.DynamicBody, true);

        this.screen = screen;

        screen.drawableSprites.add(this);
        screen.updatableObjects.add(this);
        screen.pausableObjects.add(this);
    }

    @Override
    public void update(float delta) {
        // Implement update logic for the player
        if (isPaused) {
            return; // Skip updates if the game is paused
        }

        Body body = hitbox.getBody();

        Vector2 pos = body.getPosition();

        Vector2 movDir = new Vector2(0, 0);

        if (Gdx.input.isKeyPressed(Keys.W)) {
            movDir.y += 1;
            // hello
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

        movDir.scl(1f);

        body.applyLinearImpulse(movDir, pos, true);

        Vector2 currentVelocity = body.getLinearVelocity();
        if (currentVelocity.len() > max_speed) {
            currentVelocity.nor().scl(max_speed);
            body.setLinearVelocity(currentVelocity);
        }

        screen.camera.position.set(pos.x, pos.y, 0);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Implement drawing logic for the player
    }

    @Override
    public void hide() {
        // Implement hide logic for the player if necessary
    }

    @Override
    public void pause() {
        // Implement pause logic for the player
        isPaused = true;
    }

    @Override
    public void resume() {
        // Implement resume logic for the player
        isPaused = false;
    }

    public void setPosition(float x, float y) {
        hitbox.getBody().setTransform(x, y, 0); // Set the player's position in the world
    }
}
