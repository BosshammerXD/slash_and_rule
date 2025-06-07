package io.github.slash_and_rule.Bases;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public abstract class PhysicsScreen extends BaseScreen {
    protected World world = new World(new Vector2(0, 0), true);
    protected Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();

    public PhysicsScreen(boolean debug) {
        super();
        // Initialize the Box2D world and debug renderer
        debugRenderer.setDrawBodies(debug);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        // Update the Box2D world
        camera.update();
        debugRenderer.render(world, camera.combined);
        world.step(delta, 6, 2);
    }
}
