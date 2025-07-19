package io.github.slash_and_rule;

import java.util.Random;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;

public class Globals {
        // region: Physics Categories
        public static final class Categories {
                public static final short Player = 0x0001;
                public static final short Enemy = 0x0002;
                public static final short Item = 0x0004;

                public static final short Wall = 0x0008;

                public static final short Hitbox = 0x0010;
                public static final short Sensor = 0x0020;
        }

        public static final class Masks {
                public static final short PlayerCollider = Categories.Enemy |
                                Categories.Item |
                                Categories.Wall |
                                Categories.Sensor;
                public static final short Wall = Categories.Player |
                                Categories.Enemy |
                                Categories.Item |
                                Categories.Hitbox;
                public static final short EnemyCollider = Categories.Player |
                                Categories.Wall |
                                Categories.Enemy;
        }

        // endregion
        //
        //
        //
        // region: System Priorities
        public static final class Priorities {
                public static final class Systems {

                        public static final class Draw {
                                public static final int Midfield = 120;
                                public static final int Background = 100;
                                public static final int Animation = 90;
                        }

                        public static final class Dungeon {
                                public static final int Entry = 125;
                                public static final int DungeonRoom = 80;
                                public static final int Door = 70;
                                public static final int Item = 100;

                                public static final class EnemyTypes {
                                        public static final int Jumper = 91;
                                }

                                public static final int Enemy = 90;
                                public static final int Weapon = 30;
                                public static final int Player = 20;
                        }

                        public static final class Physics {
                                public static final int Physic = 40;
                                public static final int Collision = 50;
                        }

                        public static final class Transform {
                                public static final int Movement = 70;
                        }

                        public static final int Healthbar = 130;
                        public static final int Health = 60;
                        public static final int Input = 10;
                        public static final int State = 0;
                }
        }

        // endregion
        //
        //
        //
        // region: Key Bindings
        public static final class Controls {
                public static int MoveUp = Keys.W;
                public static int MoveDown = Keys.S;
                public static int MoveLeft = Keys.A;
                public static int MoveRight = Keys.D;

                public static int Attack = Buttons.LEFT;
        }

        // endregion
        public static final class Dungeon {
                public static final class Spawner {
                        public static float interval = 1f;
                        public static float distance2 = 1f;
                }

                public static String Level = "level_1";
                public static String weapon = "BasicSword";

        }

        public static final Random random = new Random();

        public static final int GameWidth = 16;
        public static final int GameHeight = 9;
}
