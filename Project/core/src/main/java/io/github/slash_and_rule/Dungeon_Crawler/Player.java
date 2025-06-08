package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.InputManager;

import io.github.slash_and_rule.Interfaces.Displayable;
import io.github.slash_and_rule.Interfaces.Updatetable;
import io.github.slash_and_rule.Interfaces.Pausable;

import io.github.slash_and_rule.Bases.BasePhysicsObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Player extends BasePhysicsObject implements Displayable, Updatetable, Pausable {
    private boolean isPaused = false;
    private float max_speed = 10f; // Maximum speed of the player

    public Player(PhysicsScreen screen, InputManager inputManager, World world) {
        super(screen, inputManager, world, 1f, 0f, 0f, 4, 3, Globals.PlayerCategory, Globals.PlayerMask,
                BodyType.DynamicBody);

        screen.drawableSprites.add(this);
        screen.updatableObjects.add(this);
        screen.pausableObjects.add(this);

    }

    @Override
    protected CircleShape getHitboxShape() {
        CircleShape shape = new CircleShape();
        shape.setRadius(0.5f); // Set the radius of the player's hitbox
        return shape;
    }

    @Override
    public void dispose() {
        // Dispose of player resources if necessary
    }

    @Override
    public void update(float delta) {
        // Implement update logic for the player
        if (isPaused) {
            return; // Skip updates if the game is paused
        }
        Vector2 pos = body.getPosition();

        Vector2 movDir = new Vector2(0, 0);

        if (Gdx.input.isKeyPressed(Keys.D)) {
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
}
