package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Interfaces.Displayable;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.Interfaces.Updatetable;
import io.github.slash_and_rule.LoadingScreen.MsgRunnable;
import io.github.slash_and_rule.Utils.ColliderObject;
import io.github.slash_and_rule.Interfaces.Pausable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;

public class Player implements Displayable, Updatetable, Pausable, Initalizable, Disposable {
    private boolean isPaused = false;
    private float max_speed = 10f; // Maximum speed of the player

    private PhysicsScreen screen;

    private TextureAtlas playerAtlas;

    private Animation<TextureAtlas.AtlasRegion>[] moveAnimations = new Animation[4]; // nothing to see here

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
        screen.loadableObjects.add(this);
        screen.disposableObjects.add(this);
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

        movDir.scl(this.max_speed*10);

        body.setLinearVelocity(movDir);
        //body.applyLinearImpulse(movDir, pos, true);
        Vector2 currentVelocity = body.getLinearVelocity();
        if (currentVelocity.len() > max_speed) {
            currentVelocity.nor().scl(max_speed);
            body.setLinearVelocity(currentVelocity);
        }

        int lastMovIndex = movIndex;

        if (Math.abs(currentVelocity.x) > Math.abs(currentVelocity.y)) {
            if (currentVelocity.x > 0) {
                movIndex = 2;
            } else {
                movIndex = 0;
            }
        } else {
            if (currentVelocity.y > 0) {
                movIndex = 3;
            } else {
                movIndex = 1;
            }
        }

        this.stateTime += currentVelocity.len() * delta / 10f * moveAnimations[movIndex].getKeyFrames().length;

        if (movIndex != lastMovIndex) {
            this.stateTime = 0f; // Reset state time when changing direction
        }

        screen.camera.position.set(pos.x, pos.y, 0);
    }

    private int movIndex;
    private float stateTime = 0f;

    @Override
    public void draw(SpriteBatch batch) {
        // Implement drawing logic for the player
        if (isPaused) {
            return; // Skip drawing if the game is paused
        }
        TextureRegion currentFrame = moveAnimations[movIndex].getKeyFrame(stateTime, true);
        batch.draw(currentFrame, hitbox.getBody().getPosition().x - 1f, hitbox.getBody().getPosition().y - 0.5f,
                2f, 2f); // Draw the player at its position with a size of 1x1
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

    @Override
    public void init(LoadingScreen loader) {
        // TODO Auto-generated method stub
        loader.loadAsset("entities/PlayerAtlas/PLayerAtlas.atlas", TextureAtlas.class);

        loader.schedule.add(new MsgRunnable("Loading Player", () -> {
            this.playerAtlas = loader.getAssetManager().get("entities/PlayerAtlas/PLayerAtlas.atlas",
                    TextureAtlas.class);
            this.moveAnimations[0] = new Animation<>(0.2f, playerAtlas.findRegions("MoveLeft"),
                    Animation.PlayMode.LOOP);
            this.moveAnimations[2] = new Animation<>(0.2f, playerAtlas.findRegions("MoveRight"),
                    Animation.PlayMode.LOOP);
            this.moveAnimations[3] = new Animation<>(0.1f, playerAtlas.findRegions("MoveUp"), Animation.PlayMode.LOOP);
            this.moveAnimations[1] = new Animation<>(0.1f, playerAtlas.findRegions("MoveDown"),
                    Animation.PlayMode.LOOP);
        }));
    }

    @Override
    public void show(AssetManager assetManager) {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }
}
