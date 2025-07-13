package io.github.slash_and_rule.CityBuilder;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Entity;

public class CityData {
    public static Entity heldEntity = null;

    public static ArrayDeque<Entity> buildings = new ArrayDeque<>();
}
