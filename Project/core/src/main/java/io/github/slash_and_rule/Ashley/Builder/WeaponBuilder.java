package io.github.slash_and_rule.Ashley.Builder;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.TreeMap;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.MassData;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Animations.FrameData;
import io.github.slash_and_rule.Animations.triggeredAnimData;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.AnimatedComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.timedActions;
import io.github.slash_and_rule.Bases.BaseCompBuilder;
import io.github.slash_and_rule.Utils.Mappers;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

public class WeaponBuilder extends BaseCompBuilder<WeaponComponent> {
    public static class WeaponTextureData {
        public String atlasPath;
        public FrameData frames;
        public int priority;
        public float width;
        public float height;
        public float offsetX;
        public float offsetY;

        public WeaponTextureData(String atlasPath, FrameData frames, int priority, float width,
                float height, float offsetX, float offsetY) {
            this.atlasPath = atlasPath;
            this.frames = frames;
            this.priority = priority;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    private PhysicsBuilder physicsBuilder;
    private short maskBits;
    private TreeMap<Float, ArrayDeque<Runnable>> hitboxes = new TreeMap<>();
    private HashMap<WeaponComponent, WeaponTextureData> weaponTextures = new HashMap<>();

    public WeaponBuilder(PhysicsBuilder physicsBuilder, Engine engine) {
        this.physicsBuilder = physicsBuilder;

        engine.addEntityListener(Family.all(WeaponComponent.class, RenderableComponent.class, AnimatedComponent.class).get(),
                new EntityListener() {
                    public void entityAdded(Entity entity) {
                        WeaponComponent weapon = Mappers.weaponMapper.get(entity);
                        RenderableComponent renderable = Mappers.renderableMapper.get(entity);
                        AnimatedComponent animated = Mappers.animatedMapper.get(entity);

                        WeaponTextureData textureData = weaponTextures.get(weapon);
                        if (weapon == null || textureData == null) {
                            return;
                        }
                        TextureData[] textures = new TextureData[renderable.textures.length + 1];
                        System.arraycopy(renderable.textures, 0, textures, 0, renderable.textures.length);
                        weapon.texture = renderable.new TextureData(textureData.priority);
                        weapon.texture.atlasPath = textureData.atlasPath;
                        weapon.texture.width = textureData.width;
                        weapon.texture.height = textureData.height;
                        weapon.texture.offsetX = textureData.offsetX;
                        weapon.texture.offsetY = textureData.offsetY;
                        textures[renderable.textures.length] = weapon.texture;
                        renderable.textures = textures;
                        weapon.animData = new triggeredAnimData(textureData.frames, -1, weapon.texture);
                        animated.animations.put("Atk", weapon.animData);
                    };

                    public void entityRemoved(Entity entity) {
                    };
                });
    }

    public void begin(int damage, float weight, float cooldown, float chargetime, short maskBits) {
        this.hitboxes.clear();
        this.weaponTextures.clear();
        this.maskBits = maskBits;

        begin(new WeaponComponent());
        comp.body = physicsBuilder.makeBody(BodyType.DynamicBody, 0, true);

        comp.damage = damage;
        comp.weight = weight;
        comp.cooldown = cooldown;
        comp.chargetime = chargetime;
    }

    public void begin(int damage, float weight, float cooldown, short maskBits) {
        begin(damage, weight, cooldown, 0f, maskBits);
    }

    public void addHitbox(float start, float end, Shape shape) {
        checkBuilding();
        if (shape == null) {
            throw new IllegalArgumentException("Shape cannot be null.");
        }
        Fixture fixture = physicsBuilder.addFixture(comp.body, shape, 1f, (short) 0, maskBits, true);
        applyCategory(fixture, end, maskBits);
        applyCategory(fixture, start, Globals.HitboxCategory);
    }

    public void setAnimation(String atlasPath, FrameData frames, int priority, float width,
            float height, float offsetX, float offsetY) {
        checkBuilding();
        weaponTextures.put(comp,
                new WeaponTextureData(atlasPath, frames, priority, width, height, offsetX, offsetY));
    }

    @Override
    protected void finish() {
        buildFixtures();
    }

    private void buildFixtures() {
        comp.fixtures = new timedActions[hitboxes.size()];
        var indexWrapper = new Object() {
            int value = 0;
        };
        hitboxes.forEach((time, actionQueue) -> {
            Runnable[] actions = actionQueue.toArray(new Runnable[0]);
            comp.fixtures[indexWrapper.value++] = new timedActions(time, actions);
        });
        MassData massData = comp.body.getMassData();
        massData.mass = 0.001f;
        comp.body.setMassData(massData);
    }

    private void applyCategory(Fixture fixture, float time, short categoryBits) {
        ArrayDeque<Runnable> actionQueue = hitboxes.computeIfAbsent(time, k -> new ArrayDeque<>());
        actionQueue.add(() -> {
            Filter filter = fixture.getFilterData();
            filter.categoryBits = categoryBits;
            fixture.setFilterData(filter);
        });
    }
}
