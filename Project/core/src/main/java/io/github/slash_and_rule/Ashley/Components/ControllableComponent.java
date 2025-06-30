package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;

import io.github.slash_and_rule.Bases.Inputhandler;

public class ControllableComponent implements Component {
    public Inputhandler inputhandler;

    public ControllableComponent(Inputhandler inputhandler) {
        this.inputhandler = inputhandler;
    }

    public ControllableComponent() {
        this.inputhandler = null;
    }
}
