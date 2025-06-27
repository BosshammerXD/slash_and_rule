package io.github.slash_and_rule.Bases;

import java.util.ArrayDeque;
import java.util.function.Consumer;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import io.github.slash_and_rule.Ashley.Systems.InputSystem.MouseInputType;

public class Inputhandler {
    protected float delta = 0f;

    public void preEvents(Entity entity) {
    }

    public void mouseEvent(MouseInputType type, int screenX, int screenY, int button) {

    }

    public void keyEvent(int keycode, boolean pressed) {
    }

    public void keyTypedEvent(char character) {
    }

    public void scrollEvent(float amountX, float amountY) {
    }

    public void pollevent() {
    }

    private ArrayDeque<Consumer<Entity>> schedule = new ArrayDeque<>();

    public final <T extends Component> void apply(ComponentMapper<T> ComponentMapper, Consumer<T> consumer) {
        schedule.add(entity -> {
            T component = ComponentMapper.get(entity);
            if (component != null) {
                consumer.accept(component);
            }
        });
    }

    public final void finishSchedule(Entity entity) {
        while (!schedule.isEmpty()) {
            schedule.poll().accept(entity);
        }
    }

    public final void setDelta(float delta) {
        this.delta = delta;
    }
}
