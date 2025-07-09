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
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.timedActions;
import io.github.slash_and_rule.Utils.Mappers;
import io.github.slash_and_rule.Utils.PhysicsBuilder;

public class WeaponBuilder {
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
    private WeaponComponent weapon;
    private boolean building = false;
    private short maskBits;
    private TreeMap<Float, ArrayDeque<Runnable>> hitboxes = new TreeMap<>();
    private HashMap<WeaponComponent, WeaponTextureData> weaponTextures = new HashMap<>();

    public WeaponBuilder(PhysicsBuilder physicsBuilder, Engine engine) {
        this.physicsBuilder = physicsBuilder;

        engine.addEntityListener(Family.all(WeaponComponent.class, RenderableComponent.class).get(),
                new EntityListener() {
                    public void entityAdded(Entity entity) {
                        WeaponComponent weapon = Mappers.weaponMapper.get(entity);
                        RenderableComponent renderable = Mappers.renderableMapper.get(entity);
                        WeaponTextureData textureData = weaponTextures.get(weapon);
                        if (weapon == null || renderable == null || textureData == null) {
                            return;
                        }

                        weapon.animData = new triggeredAnimData(
                                textureData.atlasPath, textureData.frames, -1);

                        weapon.texture = new RenderableComponent.TextureData() {
                            {
                                texture = null;
                                animData = weapon.animData;
                                width = textureData.width;
                                height = textureData.height;
                                offsetX = textureData.offsetX;
                                offsetY = textureData.offsetY;
                            }
                        };
                        renderable.addTextureDatas(textureData.priority, weapon.texture);
                    };

                    public void entityRemoved(Entity entity) {
                    };
                });
    }

    public void begin(int damage, float weight, float cooldown, float chargetime, short maskBits) {
        this.hitboxes.clear();
        this.weaponTextures.clear();
        this.maskBits = maskBits;

        weapon = new WeaponComponent();
        weapon.body = physicsBuilder.makeBody(BodyType.DynamicBody, 0, true);

        weapon.damage = damage;
        weapon.weight = weight;
        weapon.cooldown = cooldown;
        weapon.chargetime = chargetime;

        building = true;
    }

    public void begin(int damage, float weight, float cooldown, short maskBits) {
        begin(damage, weight, cooldown, 0f, maskBits);
    }

    public void addHitbox(float start, float end, Shape shape) {
        checkBuilding();
        if (shape == null) {
            throw new IllegalArgumentException("Shape cannot be null.");
        }
        Fixture fixture = physicsBuilder.addFixture(weapon.body, shape, 1f, (short) 0, maskBits, true);
        applyCategory(fixture, end, maskBits);
        applyCategory(fixture, start, Globals.HitboxCategory);
    }

    public WeaponComponent end(String atlasPath, FrameData frames, int priority, float width,
            float height, float offsetX, float offsetY) {
        weaponTextures.put(weapon,
                new WeaponTextureData(atlasPath, frames, priority, width, height, offsetX, offsetY));
        buildFixtures();
        building = false;
        return weapon;
    }

    public void end(String atlasPath, FrameData frames, int priority, float width,
            float height, float offsetX, float offsetY, Entity entity) {
        entity.add(this.end(atlasPath, frames, priority, width, height, offsetX, offsetY));
    }

    public WeaponComponent end() {
        buildFixtures();
        building = false;
        return weapon;
    }

    public void end(Entity entity) {
        entity.add(this.end());
    }

    private void checkBuilding() {
        if (!building) {
            throw new IllegalStateException("You can only access the weaponbuilder between begin() and end().");
        }
    }

    private void buildFixtures() {
        weapon.fixtures = new timedActions[hitboxes.size()];
        var indexWrapper = new Object() {
            int value = 0;
        };
        hitboxes.forEach((time, actionQueue) -> {
            Runnable[] actions = actionQueue.toArray(new Runnable[0]);
            weapon.fixtures[indexWrapper.value++] = new timedActions(time, actions);
        });
        MassData massData = weapon.body.getMassData();
        massData.mass = 0.001f;
        weapon.body.setMassData(massData);
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
