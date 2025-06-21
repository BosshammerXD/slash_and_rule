package io.github.slash_and_rule.Ashley.Components.PhysicsComponents;

import java.util.HashMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

public class PhysicsComponent implements Component {

    public Body body;
    public HashMap<String, Fixture> fixtures = new HashMap<>();
}
