package io.github.slash_and_rule.Ashley.Components.DungeonComponents;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class DoorComponent implements Component {
    public static enum DoorType {
        LEFT(0), BOTTOM(1), RIGHT(2), TOP(3), NOTSET(-1);

        public final int value;

        DoorType(int value) {
            this.value = value;
        }
    }

    public static enum DoorState {
        ClOSED, CLOSING, OPEN, OPENING
    }

    public DoorType type = DoorType.NOTSET;
    public DoorState open = DoorState.ClOSED;
    public Entity neighbour = null;

    public DoorComponent() {
    }

    public DoorComponent(DoorType type) {
        this.type = type;
    }
}
