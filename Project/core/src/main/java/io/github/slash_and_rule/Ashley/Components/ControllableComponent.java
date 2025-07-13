package io.github.slash_and_rule.Ashley.Components;

import java.util.ArrayDeque;

import com.badlogic.ashley.core.Component;

import io.github.slash_and_rule.Ashley.Systems.InputSystem.MouseInputType;

public class ControllableComponent implements Component {
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

    public static class KeyData {
        public int keycode;
        public boolean pressed;

        public KeyData(int keycode, boolean pressed) {
            this.keycode = keycode;
            this.pressed = pressed;
        }
    }

    public static class ScrollData {
        public float amountX;
        public float amountY;

        public ScrollData(float amountX, float amountY) {
            this.amountX = amountX;
            this.amountY = amountY;
        }
    }

    public float delta = 0f;

    public ArrayDeque<MouseData> mouseQueue = new ArrayDeque<>();

    public ArrayDeque<KeyData> keyQueue = new ArrayDeque<>();

    public ArrayDeque<Character> keyTypedQueue = new ArrayDeque<>();

    public ArrayDeque<ScrollData> scrollQueue = new ArrayDeque<>();
}
