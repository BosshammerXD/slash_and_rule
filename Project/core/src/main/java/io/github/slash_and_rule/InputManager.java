package io.github.slash_and_rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.InputProcessor;

public class InputManager implements InputProcessor {
    @FunctionalInterface
    private static interface touchEvent {
        void execute(int screenX, int screenY);
    }

    @FunctionalInterface
    private static interface touchEventAdvanced {
        void execute(int screenX, int screenY, int pointer, int button);
    }

    public Map<Integer, Runnable> keyDownEvents = new HashMap<>();
    public Map<Integer, Runnable> keyUpEvents = new HashMap<>();

    public ArrayList<touchEventAdvanced> touchDownEvents = new ArrayList<>();
    public ArrayList<touchEventAdvanced> touchUpEvents = new ArrayList<>();
    // public ArrayList<Runnable> touchDraggedEvents = new ArrayList<>();
    public ArrayList<touchEvent> mouseMovedEvents = new ArrayList<>();
    public ArrayList<touchEvent> scrollEvents = new ArrayList<>();

    @Override
    public boolean keyDown(int keycode) {
        // Handle key down events
        if (keyDownEvents.containsKey(keycode)) {
            keyDownEvents.get(keycode).run();
            return true;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        // Handle key up events
        if (keyUpEvents.containsKey(keycode)) {
            keyUpEvents.get(keycode).run();
            return true;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        // Handle key typed events
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Handle touch down events
        for (touchEventAdvanced event : touchDownEvents) {
            event.execute(screenX, screenY, pointer, button);
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Handle touch up events
        for (touchEventAdvanced event : touchUpEvents) {
            event.execute(screenX, screenY, pointer, button);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Handle touch dragged events
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        // Handle touch cancelled events
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // Handle mouse moved events
        for (touchEvent event : mouseMovedEvents) {
            event.execute(screenX, screenY);
        }
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // Handle scroll events
        return false;
    }
}
