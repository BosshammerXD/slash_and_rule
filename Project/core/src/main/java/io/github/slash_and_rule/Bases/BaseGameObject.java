package io.github.slash_and_rule.Bases;

import io.github.slash_and_rule.InputManager;

public abstract class BaseGameObject {
    protected BaseScreen screen;
    protected InputManager inputManager;

    public BaseGameObject(BaseScreen screen, InputManager inputManager) {
        if (screen == null || inputManager == null) {
            throw new IllegalArgumentException("Screen and InputManager cannot be null");
        }
        this.screen = screen;
        this.inputManager = inputManager;
    }

    /**
     * This method is called when the object is no longer needed and should be
     * disposed of.
     */
    public abstract void dispose();
}
