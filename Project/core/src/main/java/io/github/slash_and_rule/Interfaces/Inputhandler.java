package io.github.slash_and_rule.Interfaces;

import com.badlogic.ashley.core.Entity;

public interface Inputhandler {
    public void handleInput(Entity entity, float deltaTime);
}
