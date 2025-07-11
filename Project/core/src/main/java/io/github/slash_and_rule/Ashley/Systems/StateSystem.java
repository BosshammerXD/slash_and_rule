package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.StateComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.StateComponent.State;
import io.github.slash_and_rule.Utils.Mappers;

public class StateSystem extends IteratingSystem {
    public StateSystem(int priority) {
        super(Family.all(StateComponent.class).get(), priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StateComponent state = Mappers.stateMapper.get(entity);
        State currentState = state.state;
        switch (currentState) {
            case State.ACTIVATE:
                activate(entity);
                break;
            case State.DEACTIVATE:
                deactivate(entity);
                break;
            default:
                break;
        }
    }

    private void activate(Entity entity) {
        entity.remove(InactiveComponent.class);
        PhysicsComponent physics = Mappers.physicsMapper.get(entity);
        if (physics != null && physics.body != null) {
            physics.body.setActive(true);
        }
    }

    private void deactivate(Entity entity) {
        entity.add(new InactiveComponent());
        PhysicsComponent physics = Mappers.physicsMapper.get(entity);
        if (physics != null && physics.body != null) {
            physics.body.setActive(false);
        }
    }
}
