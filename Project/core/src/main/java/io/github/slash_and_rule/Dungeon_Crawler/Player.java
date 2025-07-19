package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Animations.FrameData;
import io.github.slash_and_rule.Animations.MovingEntityAnimData;
import io.github.slash_and_rule.Ashley.Builder.PhysCompBuilder;
import io.github.slash_and_rule.Ashley.Builder.WeaponBuilder;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.AnimatedComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Utils.ShapeBuilder;
import io.github.slash_and_rule.Utils.UtilFuncs;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Player {
    private PhysCompBuilder physCompBuilder;
    private WeaponBuilder weaponBuilder;

    public Player(PhysCompBuilder physCompBuilder, WeaponBuilder weaponBuilder) {
        this.physCompBuilder = physCompBuilder;
        this.weaponBuilder = weaponBuilder;
    }

    public Entity init() {
        Entity player = new Entity();

        player.add(new PlayerComponent());

        TransformComponent traC = new TransformComponent();
        player.add(traC);

        // region: MidfieldComp
        String atlasPath = UtilFuncs.getEntityAtlas("Player");

        MidfieldComponent midC = new MidfieldComponent();

        TextureData moveTextureData = midC.new TextureData(1);
        moveTextureData.atlasPath = atlasPath;
        moveTextureData.width = MidfieldComponent.dynamicValue;
        moveTextureData.height = MidfieldComponent.dynamicValue;
        moveTextureData.offsetX = 0;
        moveTextureData.offsetY = 0.5f;
        moveTextureData.scale = 1 / 16f;

        TextureData capeTextureData = midC.new TextureData(2);
        capeTextureData.atlasPath = atlasPath;
        capeTextureData.width = MidfieldComponent.dynamicValue;
        capeTextureData.height = MidfieldComponent.dynamicValue;
        capeTextureData.offsetX = 0;
        capeTextureData.offsetY = 0.5f;
        capeTextureData.scale = 1 / 16f;

        midC.textures = new TextureData[] { moveTextureData, capeTextureData };
        player.add(midC);
        // endregion
        //
        //
        //
        // region: AnimatedComp
        AnimatedComponent aniC = new AnimatedComponent();
        aniC.animations.put("Move", new MovingEntityAnimData(atlasPath, playerFrameDatas(), moveTextureData));
        aniC.animations.put("CapeMove", new MovingEntityAnimData(atlasPath, capeFrameDatas(), capeTextureData));
        player.add(aniC);
        // endregion
        //
        //
        //
        // region: PhysicsComp
        float colliderRadius = 7 / 16f;
        Shape colliderShape = ShapeBuilder.circ(colliderRadius);

        float hurtBoxWidth = 5 / 16f;
        float hurtBoxHeight = 11 / 16f;
        float hurtBoxOffsetX = 0;
        float hurtBoxOffsetY = 0.5f;
        Shape hurtBoxShape = ShapeBuilder.rect(hurtBoxOffsetX, hurtBoxOffsetY, hurtBoxWidth, hurtBoxHeight);

        float friction = 7.5f;
        float density = 1f;
        physCompBuilder.begin(traC.position, BodyType.DynamicBody, friction);
        physCompBuilder.getBody().setFixedRotation(true);
        physCompBuilder.add("Collider", colliderShape, density,
                Globals.PlayerCategory, Globals.ColPlayerMask, PhysCompBuilder.NotSensor);
        physCompBuilder.add("HurtBox", hurtBoxShape,
                Globals.PlayerCategory, Globals.HitboxCategory, PhysCompBuilder.IsSensor);
        physCompBuilder.end(player);
        // endregion
        //
        //
        //
        // region: WeaponComp
        weaponBuilder.begin(10, 10, 0.5f, Globals.EnemyCategory);

        Shape hitbox = ShapeBuilder.poly(
                new float[] { 0.5f, 0f, 1.2f, 1f, 1.7f, 0f, 1.2f, -1f });

        weaponBuilder.addHitbox(.1f, 0.3f, hitbox);

        FrameData frames = new FrameData(4, 0.1f, "AtkAnim");

        weaponBuilder.setAnimation(UtilFuncs.getWeaponAtlas("BasicSword"), frames, 0,
                3f, 3f, -0.9f, -0.5f);

        weaponBuilder.end(player);
        // endregion
        //
        //
        //
        // region: MovementComp
        MovementComponent movC = new MovementComponent();
        movC.max_speed = 10f;
        player.add(movC);
        // endregion
        //
        //
        //
        // region: HealthComp
        HealthComponent heaC = new HealthComponent();
        heaC.maxHealth = 100;
        heaC.health = 100;
        heaC.offsetY = 1.5f;
        player.add(heaC);
        // endregion

        player.add(new ControllableComponent());

        System.out.println(player.getComponents());
        return player;
    }

    private final float defaultFrameTime = 0.1f;
    private final int[] numFramesPerDirIdle = new int[] { 1, 1, 1, 1 };
    private final int[] numFramesPerDirMove = new int[] { 10, 4, 10, 4 };
    private final int[] numFramesPerDirAtk = new int[] { 4, 4, 4, 4 };

    private FrameData[][] playerFrameDatas() {
        FrameData[][] frameDatas = new FrameData[3][];
        frameDatas[0] = FrameData.createMultiple(numFramesPerDirIdle, UtilFuncs.getDirs("Move"), defaultFrameTime);
        frameDatas[1] = FrameData.createMultiple(numFramesPerDirMove, UtilFuncs.getDirs("Move"), defaultFrameTime);
        frameDatas[1][1].mult(2);
        frameDatas[1][3].mult(2);
        frameDatas[2] = FrameData.createMultiple(numFramesPerDirAtk, UtilFuncs.getDirs("Atk"), defaultFrameTime);
        return frameDatas;
    }

    private FrameData[][] capeFrameDatas() {
        FrameData[][] frameDatas = new FrameData[3][];
        frameDatas[0] = FrameData.createMultiple(numFramesPerDirIdle, UtilFuncs.getDirs("CapeMove"), defaultFrameTime);
        frameDatas[1] = FrameData.createMultiple(numFramesPerDirMove, UtilFuncs.getDirs("CapeMove"), defaultFrameTime);
        frameDatas[1][1].mult(2);
        frameDatas[1][3].mult(2);
        frameDatas[2] = FrameData.createMultiple(numFramesPerDirAtk, UtilFuncs.getDirs("CapeAtk"), defaultFrameTime);
        return frameDatas;
    }
}
