package io.github.slash_and_rule.Ashley.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class ParentComponent implements Component {
    public Entity[] children;
}
