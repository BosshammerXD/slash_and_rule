package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Animations.FrameData;
import io.github.slash_and_rule.Animations.MovingEntityAnimData;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Builder.CompBuilders;
import io.github.slash_and_rule.Ashley.Builder.PhysCompBuilder;
import io.github.slash_and_rule.Ashley.Builder.RenderBuilder;
import io.github.slash_and_rule.Ashley.Builder.WeaponBuilder;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.AnimatedComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.SensorComponent;
import io.github.slash_and_rule.Utils.Mappers;
import io.github.slash_and_rule.Utils.ShapeBuilder;
import io.github.slash_and_rule.Utils.UtilFuncs;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Player {
    private MovingEntityAnimData moveAnimData;
    private TextureData capeTextureData;

    private PhysCompBuilder physCompBuilder;
    private WeaponBuilder weaponBuilder;
    private RenderBuilder<MidfieldComponent> renderBuilder = new RenderBuilder<MidfieldComponent>();
    private EntityManager entityManager;

    public Player(PhysCompBuilder physCompBuilder, WeaponBuilder weaponBuilder,
            EntityManager entityManager) {
        this.physCompBuilder = physCompBuilder;
        this.entityManager = entityManager;
        this.weaponBuilder = weaponBuilder;
    }

    public void init() {
        Entity entity = entityManager.reset();

        TransformComponent tC = CompBuilders.buildTransform(new Vector2(2, 2), 0).get();

        String atlasPath = UtilFuncs.getEnAtlas("Player");

        renderBuilder.begin(new MidfieldComponent());
        TextureData moveTextureData = renderBuilder.add(atlasPath, 1, 2f, 2f, -1f, -0.5f);
        this.capeTextureData = renderBuilder.add(atlasPath, 2, 2f, 2f, -1f, -0.5f);
        renderBuilder.end(entity);

        this.moveAnimData = new MovingEntityAnimData(atlasPath, playerFrameDatas(), moveTextureData);

        AnimatedComponent aC = new AnimatedComponent();
        aC.animations.put("Move", moveAnimData);
        aC.animations.put("CapeMove",
                new MovingEntityAnimData(atlasPath, capeFrameDatas(), capeTextureData));

        makePhysComp(entity, tC);

        makeWeapon(entity);

        CompBuilders.buildMovement(10f).add(entity);
        CompBuilders.buildHealth(100).add(entity);

        entityManager.build(new PlayerComponent(), new SensorComponent(), tC, new ControllableComponent(), aC);
        entityManager.finish();
        System.out.println(entity.getComponents().toString());
    }

    private void makeWeapon(Entity entity) {
        weaponBuilder.begin(10, 10, 0.5f, Globals.EnemyCategory);

        Shape hitbox = ShapeBuilder.poly(
                new float[] { 0.5f, 0f, 1.2f, 1f, 1.7f, 0f, 1.2f, -1f });

        weaponBuilder.addHitbox(.1f, 0.3f, hitbox);

        FrameData frames = new FrameData(4, 0.1f, "AtkAnim");

        weaponBuilder.setAnimation("weapons/BasicSword/BasicSword.atlas", frames, 0,
                3f, 3f, -0.9f, -0.5f);

        weaponBuilder.end(entity);
    }

    public void makePhysComp(Entity entity, TransformComponent tC) {
        CircleShape colliderShape = new CircleShape();
        colliderShape.setRadius(7 / 16f);

        PolygonShape hurtBoxShape = new PolygonShape();
        hurtBoxShape.setAsBox(5 / 16f, 11 / 16f, new Vector2(0, 0.5f), 0);

        physCompBuilder.begin(tC.position, BodyType.DynamicBody, 7.5f, true);
        physCompBuilder.getBody().setFixedRotation(true);
        physCompBuilder.add("Collider", colliderShape, 1f, Globals.PlayerCategory, Globals.ColPlayerMask, false);
        physCompBuilder.add("HurtBox", hurtBoxShape, Globals.PlayerCategory, Globals.HitboxCategory, true);
        physCompBuilder.end(entity);
    }

    public void makeAnimation(Entity entity) {
        AnimatedComponent animatedComponent = Mappers.animatedMapper.get(entity);
        if (animatedComponent == null) {
            System.out.println("Player: Entity does not have an AnimatedComponent");
            return;
        }
        animatedComponent.animations.put("Move", moveAnimData);
        animatedComponent.animations.put("CapeMove",
                new MovingEntityAnimData(UtilFuncs.getEnAtlas("Player"), capeFrameDatas(), capeTextureData));
    }

    private FrameData[][] playerFrameDatas() {
        FrameData[][] frameDatas = new FrameData[3][];
        frameDatas[0] = FrameData.createMultiple(new int[] { 1, 1, 1, 1 }, UtilFuncs.getDirs("Move"), 0.1f);
        frameDatas[0][1].mult(2);
        frameDatas[0][3].mult(2);
        frameDatas[1] = FrameData.createMultiple(new int[] { 10, 4, 10, 4 }, UtilFuncs.getDirs("Move"), 0.1f);
        frameDatas[1][1].mult(2);
        frameDatas[1][3].mult(2);
        frameDatas[2] = FrameData.createMultiple(new int[] { 10, 4, 10, 4 }, UtilFuncs.getDirs("Move"), 0.1f);
        frameDatas[2][1].mult(2);
        frameDatas[2][3].mult(2);
        return frameDatas;
    }

    private FrameData[][] capeFrameDatas() {
        FrameData[][] frameDatas = new FrameData[3][];
        frameDatas[0] = FrameData.createMultiple(new int[] { 1, 1, 1, 1 }, UtilFuncs.getDirs("CapeMove"), 0.1f);
        frameDatas[0][1].mult(2);
        frameDatas[0][3].mult(2);
        frameDatas[1] = FrameData.createMultiple(new int[] { 10, 4, 10, 4 }, UtilFuncs.getDirs("CapeMove"), 0.1f);
        frameDatas[1][1].mult(2);
        frameDatas[1][3].mult(2);
        frameDatas[2] = FrameData.createMultiple(new int[] { 10, 4, 10, 4 }, UtilFuncs.getDirs("CapeMove"), 0.1f);
        frameDatas[2][1].mult(2);
        frameDatas[2][3].mult(2);
        return frameDatas;
    }
}
