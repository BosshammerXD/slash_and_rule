package io.github.slash_and_rule.Ashley;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.math.Vector2;

public class Signals {
    public static final class RoomOpenEvent {
        public Entity roomEntity;

        public RoomOpenEvent(Entity roomEntity) {
            this.roomEntity = roomEntity;
        }
    }

    public static final Signal<RoomOpenEvent> roomOpenSignal = new Signal<>();

    public static final class PlayerTeleportEvent {
        public Vector2 position;

        public PlayerTeleportEvent(Vector2 position) {
            this.position = position;
        }
    }

    public static final Signal<PlayerTeleportEvent> playerTeleportSignal = new Signal<>();

    public static final class PlaceBuildingEvent {
        public Entity buildingEntity;

        public PlaceBuildingEvent(Entity buildingEntity) {
            this.buildingEntity = buildingEntity;
        }
    }

    public static final Signal<PlaceBuildingEvent> placeBuildingSignal = new Signal<>();
}
