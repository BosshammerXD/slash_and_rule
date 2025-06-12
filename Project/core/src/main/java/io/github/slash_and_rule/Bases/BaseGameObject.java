package io.github.slash_and_rule.Bases;

public abstract class BaseGameObject {
    protected BaseScreen screen;

    public BaseGameObject(BaseScreen screen) {
        if (screen == null) {
            throw new IllegalArgumentException("Screen and InputManager cannot be null");
        }
        this.screen = screen;
    }
}
