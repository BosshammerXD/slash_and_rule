package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Utils.Mappers;

public class InputSystem extends IteratingSystem {
    public InputSystem(int priority) {
        super(Family.all(ControllableComponent.class).get(), priority);
    }

    @Override
    protected void processEntity(com.badlogic.ashley.core.Entity entity, float deltaTime) {
        ControllableComponent controllable = Mappers.controllableMapper.get(entity);
        if (controllable != null && controllable.inputhandler != null) {
            controllable.inputhandler.handleInput(entity, deltaTime);
        }
    }
}
