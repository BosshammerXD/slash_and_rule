package io.github.slash_and_rule.Ashley;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;

public class Signals {
    public static class RoomOpenEvent {
        public Entity roomEntity;

        public RoomOpenEvent(Entity roomEntity) {
            this.roomEntity = roomEntity;
        }
    }

    public static Signal<RoomOpenEvent> roomOpenSignal = new Signal<>();
}
