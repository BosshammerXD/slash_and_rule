package io.github.slash_and_rule.Ashley.Systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.ParentComponent;
import io.github.slash_and_rule.Ashley.Components.StateComponent;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Components.StateComponent.State;
import io.github.slash_and_rule.Utils.Mappers;

public class StateSystem extends IteratingSystem {
    public StateSystem() {
        super(Family.all(StateComponent.class).get(), Globals.StateSystemPriority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StateComponent state = Mappers.stateMapper.get(entity);
        State currentState = state.state;
        switch (currentState) {
            case State.ACTIVATE:
                activate(entity);
                state.stateChanged = true;
                state.state = State.ACTIVE;
                break;
            case State.DEACTIVATE:
                deactivate(entity);
                state.stateChanged = true;
                state.state = State.INACTIVE;
                break;
            case State.ACTIVE:
            case State.INACTIVE:
                state.stateChanged = false;
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
        ParentComponent parent = Mappers.parentMapper.get(entity);
        if (parent != null) {
            for (Entity child : parent.children) {
                StateComponent childState = Mappers.stateMapper.get(child);
                activate(child);
                if (childState != null) {
                    childState.stateChanged = true;
                    childState.state = State.ACTIVE;
                }
            }
        }
    }

    private void deactivate(Entity entity) {
        entity.add(new InactiveComponent());
        PhysicsComponent physics = Mappers.physicsMapper.get(entity);
        if (physics != null && physics.body != null) {
            physics.body.setActive(false);
        }
        ParentComponent parent = Mappers.parentMapper.get(entity);
        if (parent != null) {
            for (Entity child : parent.children) {
                deactivate(child);
                StateComponent childState = Mappers.stateMapper.get(child);
                if (childState != null) {
                    childState.stateChanged = true;
                    childState.state = State.INACTIVE;
                }
            }
        }
    }
}
