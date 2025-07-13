package io.github.slash_and_rule.Ashley.Components.CityComponents;

import java.util.HashMap;

import com.badlogic.ashley.core.Component;

public class BuyableComponent implements Component {
    public HashMap<String, Integer> cost = new HashMap<>();
}
