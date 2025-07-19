package io.github.slash_and_rule.Ashley.Systems;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent.KeyData;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent.MouseData;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent.ScrollData;
import io.github.slash_and_rule.Utils.Mappers;

public class InputSystem extends EntitySystem {
    public static enum MouseInputType {
        DOWN, UP, DRAGGED, MOVED, CANCELLED
    }

    private ImmutableArray<Entity> controllables;

    private ArrayDeque<MouseData> mouseQueue = new ArrayDeque<>();
    private ArrayDeque<KeyData> keyQueue = new ArrayDeque<>();
    private ArrayDeque<Character> keyTypedQueue = new ArrayDeque<>();
    private ArrayDeque<ScrollData> scrollQueue = new ArrayDeque<>();

    private Inputcollector inputcollector = new Inputcollector();

    public void setInputProcessor() {
        Gdx.input.setInputProcessor(inputcollector);
    }

    public InputSystem() {
        super(Globals.Priorities.Systems.Input);
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
            controllable.delta = deltaTime;

            setList(controllable.mouseQueue, mouseQueue);
            setList(controllable.keyQueue, keyQueue);
            setList(controllable.keyTypedQueue, keyTypedQueue);
            setList(controllable.scrollQueue, scrollQueue);
        }

        mouseQueue.clear();
        keyQueue.clear();
        keyTypedQueue.clear();
        scrollQueue.clear();
    }

    private <T> void setList(ArrayDeque<T> list, ArrayDeque<T> newList) {
        list.clear();
        list.addAll(newList);
    }

    private class Inputcollector implements InputProcessor {
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
