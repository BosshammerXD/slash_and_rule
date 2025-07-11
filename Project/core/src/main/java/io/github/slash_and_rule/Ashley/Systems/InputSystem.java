package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Bases.Inputhandler;
import io.github.slash_and_rule.Utils.Mappers;

public class InputSystem extends EntitySystem {
    public static enum MouseInputType {
        DOWN, UP, DRAGGED, MOVED, CANCELLED
    }

    private ImmutableArray<Entity> controllables;

    private Inputcollector inputcollector = new Inputcollector();

    public void setInputProcessor() {
        Gdx.input.setInputProcessor(inputcollector);
    }

    public InputSystem(int priority) {
        super(priority);
    }

    @Override
    public void addedToEngine(Engine engine) {
        controllables = engine
                .getEntitiesFor(Family.all(ControllableComponent.class).exclude(InactiveComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : controllables) {
            ControllableComponent controllable = Mappers.controllableMapper.get(entity);
            controllable.inputhandler.setDelta(deltaTime);
            controllable.inputhandler.preEvents(entity);
            sendEvents(controllable.inputhandler);
            controllable.inputhandler.finishSchedule(entity);
        }

        inputcollector.mouseQueue.clear();
        inputcollector.keyQueue.clear();
        inputcollector.keyTypedQueue.clear();
        inputcollector.scrollQueue.clear();
    }

    private void sendEvents(Inputhandler inputhandler) {
        for (Inputcollector.MouseData data : inputcollector.mouseQueue) {
            inputhandler.mouseEvent(data.type, data.screenX, data.screenY, data.button);
        }
        for (Inputcollector.KeyData data : inputcollector.keyQueue) {
            inputhandler.keyEvent(data.keycode, data.pressed);
        }
        for (Character character : inputcollector.keyTypedQueue) {
            inputhandler.keyTypedEvent(character);
        }
        for (Inputcollector.ScrollData data : inputcollector.scrollQueue) {
            inputhandler.scrollEvent(data.amountX, data.amountY);
        }
        inputhandler.pollevent();
    }

    private class Inputcollector implements InputProcessor {
        ArrayDeque<MouseData> mouseQueue = new ArrayDeque<>();

        public static class MouseData {
            public MouseInputType type;
            public int screenX;
            public int screenY;
            public int button; // -1 for no button

            public MouseData(MouseInputType type, int screenX, int screenY, int button) {
                this.type = type;
                this.screenX = screenX;
                this.screenY = screenY;
                this.button = button;
            }
        }

        ArrayDeque<KeyData> keyQueue = new ArrayDeque<>();

        public static class KeyData {
            public int keycode;
            public boolean pressed;

            public KeyData(int keycode, boolean pressed) {
                this.keycode = keycode;
                this.pressed = pressed;
            }
        }

        ArrayDeque<Character> keyTypedQueue = new ArrayDeque<>();

        ArrayDeque<ScrollData> scrollQueue = new ArrayDeque<>();

        public static class ScrollData {
            public float amountX;
            public float amountY;

            public ScrollData(float amountX, float amountY) {
                this.amountX = amountX;
                this.amountY = amountY;
            }
        }

        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            mouseQueue.add(new MouseData(MouseInputType.CANCELLED, screenX, screenY, button));
            return true;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            mouseQueue.add(new MouseData(MouseInputType.DOWN, screenX, screenY, button));
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            mouseQueue.add(new MouseData(MouseInputType.UP, screenX, screenY, button));
            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            mouseQueue.add(new MouseData(MouseInputType.DRAGGED, screenX, screenY, -1));
            return true;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            mouseQueue.add(new MouseData(MouseInputType.MOVED, screenX, screenY, -1));
            return true;
        }

        @Override
        public boolean keyDown(int keycode) {
            keyQueue.add(new KeyData(keycode, true));
            return true;
        }

        @Override
        public boolean keyUp(int keycode) {
            keyQueue.add(new KeyData(keycode, false));
            return true;
        }

        @Override
        public boolean keyTyped(char character) {
            keyTypedQueue.add(character);
            return true;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            scrollQueue.add(new ScrollData(amountX, amountY));
            return true;
        }
    }
}
