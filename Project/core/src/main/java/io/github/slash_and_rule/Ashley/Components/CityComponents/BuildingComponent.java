package io.github.slash_and_rule.Ashley.Components.CityComponents;

import com.badlogic.ashley.core.Component;

public class BuildingComponent implements Component {
    public String name;

    public BuildingComponent copy() {
        BuildingComponent copy = new BuildingComponent();
        copy.name = this.name;
        return copy;
    }
}
