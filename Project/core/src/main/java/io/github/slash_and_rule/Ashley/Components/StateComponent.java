package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;

public class StateComponent implements Component {
    public static enum State {
        INACTIVE, // Entity is not active
        ACTIVATE, // Entity is scheduled for activation
        ACTIVE, // Entity is active
        DEACTIVATE // Entity is scheduled for deactivation
    }

    public State state = State.ACTIVATE;
    public boolean stateChanged = false;

    public StateComponent() {
    }

}
