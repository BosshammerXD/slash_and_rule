package io.github.slash_and_rule;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;

public class Globals {
        // region: Physics Categories
        /**
         * Physics categories for collision detection.
         * These categories are used to define which objects can collide with each
         * other.
         */
        public static final short PlayerCategory = 0x0001;
        public static final short EnemyCategory = 0x0002;
        public static final short ItemCategory = 0x0004;

        public static final short WallCategory = 0x0008;

        public static final short HitboxCategory = 0x0010;
        public static final short SensorCategory = 0x0020;

        public static final short PlayerMask = EnemyCategory | ItemCategory | WallCategory | SensorCategory;
        public static final short WallMask = PlayerCategory | EnemyCategory | ItemCategory | HitboxCategory;

        // endregion
        //
        //
        //
        // region: System Priorities
        /**
         * System priorities for the Ashley framework.
         * These priorities determine the order in which systems are updated.
         */
        public static final int RenderSystemPriority = 100;
        public static final int AnimationSystemPriority = 90;
        public static final int MovementSystemPriority = 70;
        public static final int DungeonSystemPriority = 60;
        public static final int CollisionSystemPriority = 50;
        public static final int PhysicsSystemPriority = 40;
        public static final int WeaponSystemPriority = 30;
        public static final int InputSystemPriority = 10;

        // endregion
        //
        //
        //
        // region: Key Bindings
        /**
         * Key bindings for the game.
         * These keys are used for player input and control.
         */
        public static int MoveUpKey = Keys.W;
        public static int MoveDownKey = Keys.S;
        public static int MoveLeftKey = Keys.A;
        public static int MoveRightKey = Keys.D;

        public static int AttackButton = Buttons.LEFT;

        // endregion
}
