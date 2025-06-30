package io.github.slash_and_rule.Animations;

import java.util.function.Supplier;

import com.badlogic.gdx.math.Vector2;

public class MovementAnimData extends LoopedAnimData {
    private Supplier<Vector2> movementVector;
    private String[] animationNames;
    private float[] frameDurations;
    private int nameIndex = 0;

    public MovementAnimData(String atlasPath, Supplier<Vector2> movVecSupplier, String[] animNames, float[] frameDurations) {
        super(atlasPath, animNames[0], frameDurations[0]);
        this.movementVector = movVecSupplier;
        this.animationNames = animNames;
        this.frameDurations = frameDurations;
    }

    @Override
    public void update(float deltaTime) {
        Vector2 movementVector = this.movementVector.get();
        if (movementVector.isZero() || movementVector.len() < 0.01f) {
            stateTime = 0f; // Reset animation time if no movement
            animIndex = 0;
        }

        int lastDir = nameIndex;

        float absX = Math.abs(movementVector.x);
        float absY = Math.abs(movementVector.y);

        float range = frameDurations[nameIndex] * deltaTime;

        if (Math.abs(absX - absY) < range) {
            if (movementVector.y > range) {
                nameIndex = (lastDir == 1) ? 3 : lastDir;
            } else if (movementVector.y < -range) {
                nameIndex = (lastDir == 3) ? 1 : lastDir;
            }
        } else if (absX > absY) {
            nameIndex = (movementVector.x > 0) ? 2 : 0; // Right or Left
        } else {
            nameIndex = (movementVector.y > 0) ? 3 : 1; // Up or Down
        }

        if (lastDir != nameIndex) {
            stateTime = 0f; // Reset animation time when changing direction
        } else {
            stateTime += deltaTime; // Update animation time
        }

        name = animationNames[nameIndex];
        frameDuration = frameDurations[nameIndex];

        super.update(deltaTime);
    }
}
