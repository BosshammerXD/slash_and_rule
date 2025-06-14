package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.PhysicsScreen;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.InputManager;
import io.github.slash_and_rule.LoadingScreen;
import io.github.slash_and_rule.Animations.MovementAnimation;
import io.github.slash_and_rule.Animations.MovementAnimation.AnimData;
import io.github.slash_and_rule.Interfaces.SortedDisplayable;
import io.github.slash_and_rule.Interfaces.Initalizable;
import io.github.slash_and_rule.Interfaces.Updatetable;
import io.github.slash_and_rule.LoadingScreen.MsgRunnable;
import io.github.slash_and_rule.Utils.ColliderObject;
import io.github.slash_and_rule.Interfaces.Pausable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;

public class Player implements SortedDisplayable, Updatetable, Pausable, Initalizable, Disposable {
    private float max_speed = 10f; // Maximum speed of the player

    private PhysicsScreen screen;

    private MovementAnimation moveAnimation;
    private MovementAnimation capeMoveAnimation;

    private ColliderObject hitbox;

    public Player(PhysicsScreen screen, InputManager inputManager) {
        CircleShape hitboxShape = new CircleShape();
        hitboxShape.setRadius(7 / 16f); // Set the radius of the player's hitbox
        this.hitbox = new ColliderObject(screen, 1f, 7.5f, 0f, 2.5f, 2.5f, Globals.PlayerCategory, Globals.PlayerMask,
                hitboxShape, BodyType.DynamicBody, true);

        this.screen = screen;

        screen.sortedDrawableObjects.add(this);
        screen.updatableObjects.add(this);
        screen.pausableObjects.add(this);
        screen.loadableObjects.add(this);
        screen.disposableObjects.add(this);
    }

    private Vector2 lastPos = new Vector2(0, 0);

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
        // body.applyLinearImpulse(movDir, pos, true);
        // Vector2 currentVelocity = body.getLinearVelocity();
        // if (currentVelocity.len() > max_speed) {
        // currentVelocity.nor().scl(max_speed);
        // body.setLinearVelocity(currentVelocity);
        // }

        Vector2 moveVec = new Vector2(pos.x - lastPos.x, pos.y - lastPos.y);

        moveAnimation.update(moveVec, delta);
        capeMoveAnimation.update(moveVec, delta);

        lastPos.set(pos);

        screen.camera.position.set(pos.x, pos.y, 0);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Implement drawing logic for the player
        Vector2 pos = hitbox.getBody().getPosition();
        float x = pos.x - 1f;
        float y = pos.y - 0.5f;

        if (moveAnimation.getDir() == 1) {
            batch.draw(capeMoveAnimation.getFrame(), x, y, 2f, 2f);
            batch.draw(moveAnimation.getFrame(), x, y, 2f, 2f);
        } else {
            batch.draw(moveAnimation.getFrame(), x, y, 2f, 2f);
            batch.draw(capeMoveAnimation.getFrame(), x, y, 2f, 2f);
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

    public void setPosition(float x, float y) {
        hitbox.getBody().setTransform(x, y, 0); // Set the player's position in the world
    }

    @Override
    public void init(LoadingScreen loader) {
        // TODO Auto-generated method stub
        loader.loadAsset("entities/PlayerAtlas/PLayerAtlas.atlas", TextureAtlas.class);

        loader.schedule.add(new MsgRunnable("Loading Player", () -> {
            this.moveAnimation = new MovementAnimation(loader.getAssetManager(),
                    "entities/PlayerAtlas/PLayerAtlas.atlas",
                    new AnimData[] {
                            new AnimData("MoveLeft", 0.05f),
                            new AnimData("MoveDown", 0.1f),
                            new AnimData("MoveRight", 0.05f),
                            new AnimData("MoveUp", 0.1f)
                    });
            this.capeMoveAnimation = new MovementAnimation(loader.getAssetManager(),
                    "entities/PlayerAtlas/PLayerAtlas.atlas",
                    new AnimData[] {
                            new AnimData("CapeMoveLeft", 0.05f),
                            new AnimData("CapeMoveDown", 0.1f),
                            new AnimData("CapeMoveRight", 0.05f),
                            new AnimData("CapeMoveUp", 0.1f)
                    });
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

    @Override
    public float getSortIndex() {
        return this.hitbox.getBody().getPosition().y; // Use the y position for sorting
    }
}
