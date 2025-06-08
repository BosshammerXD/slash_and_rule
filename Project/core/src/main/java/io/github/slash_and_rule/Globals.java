package io.github.slash_and_rule;

public class Globals {
    public static final short PlayerCategory = 0x0001;
    public static final short EnemyCategory = 0x0002;
    public static final short ItemCategory = 0x0004;

    public static final short WallCategory = 0x0008;

    public static final short ProjectileCategory = 0x0010;
    public static final short PlayerSensorCategory = 0x0020;
    public static final short PlayerProjectileCategory = 0x0040;

    public static final short PlayerMask = EnemyCategory | ItemCategory | WallCategory | ProjectileCategory
            | PlayerSensorCategory;
    public static final short WallMask = PlayerCategory | EnemyCategory | ItemCategory | ProjectileCategory
            | PlayerProjectileCategory;
}
