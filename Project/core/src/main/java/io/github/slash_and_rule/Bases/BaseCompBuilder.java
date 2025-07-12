package io.github.slash_and_rule.Bases;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public abstract class BaseCompBuilder<T extends Component> {
    protected T comp;
    private boolean building = false;

    protected void begin(T component) {
        if (building) {
            throw new IllegalStateException("You can't call begin() while building.");
        }
        this.comp = component;
        building = true;
    }

    public T end() {
        finish();
        building = false;
        T comp = this.comp;
        this.comp = null;
        return comp;
    }

    public final void end(Entity entity) {
        entity.add(end());
    }

    protected void finish() {
    }

    protected void checkBuilding() {
        if (!building) {
            throw new IllegalStateException(
                    "You can only access the " + getClass().getSimpleName() + " between begin() and end().");
        }
    }
}
