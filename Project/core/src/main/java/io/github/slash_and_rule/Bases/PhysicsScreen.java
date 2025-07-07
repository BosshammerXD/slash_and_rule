package io.github.slash_and_rule.Bases;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Systems.CollisionSystem;
import io.github.slash_and_rule.Ashley.Systems.PhysicsSystem;
import io.github.slash_and_rule.Utils.AtlasManager;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

public abstract class PhysicsScreen extends GameScreen {
    protected World world = new World(new Vector2(0, 0), true);
    protected Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    protected PhysicsBuilder physicsBuilder = new PhysicsBuilder(world);

    public PhysicsScreen(AssetManager assetManager, AtlasManager atlasManager, boolean debug) {
        super(assetManager, atlasManager);
        // Initialize the Box2D world and debug renderer
        // world.setContactListener(contactListener);
        debugRenderer.setDrawBodies(debug);

        engine.addSystem(new PhysicsSystem(Globals.PhysicsSystemPriority, world));
        engine.addSystem(new CollisionSystem(Globals.CollisionSystemPriority, world));
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (halt) {
            return; // Skip rendering if the screen is halted
        }
        // Update the Box2D world
        camera.update();
        debugRenderer.render(world, camera.combined);
    }

    public World getWorld() {
        return world;
    }

    public PhysicsBuilder getPhysicsBuilder() {
        return physicsBuilder;
    }
}
