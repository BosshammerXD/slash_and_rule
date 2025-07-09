package io.github.slash_and_rule.Dungeon_Crawler;

import io.github.slash_and_rule.Bases.Inputhandler;
import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Animations.FrameData;
import io.github.slash_and_rule.Animations.mobEnAnimData;
import io.github.slash_and_rule.Ashley.EntityManager;
import io.github.slash_and_rule.Ashley.Builder.WeaponBuilder;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.HealthComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.MidfieldComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.RenderableComponent.TextureData;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.WeaponStates;
import io.github.slash_and_rule.Ashley.Components.PhysicsComponents.PhysicsComponent;
import io.github.slash_and_rule.Ashley.Systems.InputSystem.MouseInputType;
import io.github.slash_and_rule.Utils.Mappers;
import io.github.slash_and_rule.Utils.PhysicsBuilder;
import io.github.slash_and_rule.Utils.ShapeBuilder;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Player {

    private TextureData moveTextureData;
    private TextureData capeTextureData;

    private PhysicsBuilder physicsBuilder;
    private WeaponBuilder weaponBuilder;
    private OrthographicCamera camera;
    private EntityManager entityManager;

    public Player(PhysicsBuilder physicsBuilder, WeaponBuilder weaponBuilder, OrthographicCamera camera,
            EntityManager entityManager) {
        this.physicsBuilder = physicsBuilder;
        this.camera = camera;
        this.entityManager = entityManager;
        this.weaponBuilder = weaponBuilder;
    }

    public void init() {
        CircleShape colliderShape = new CircleShape();
        colliderShape.setRadius(7 / 16f);

        PolygonShape hurtBoxShape = new PolygonShape();
        hurtBoxShape.setAsBox(5 / 16f, 11 / 16f, new Vector2(0, 0.5f), 0);

        Entity entity = entityManager.reset();

        TransformComponent tC = new TransformComponent(
                new Vector2(2, 2), 0f);

        RenderableComponent rC = new RenderableComponent();
        mobEnAnimData moveAnimData = new mobEnAnimData(
                "entities/Player/Player.atlas", playerFrameDatas());
        this.moveTextureData = new TextureData() {
            {
                animData = moveAnimData;
                width = 2f;
                height = 2f;
                offsetX = -1f;
                offsetY = -0.5f;
            }
        };
        rC.addTextureDatas(1, this.moveTextureData);
        mobEnAnimData capeMoveAnimData = new mobEnAnimData(
                "entities/Player/Player.atlas", capeFrameDatas());

        this.capeTextureData = new TextureData() {
            {
                animData = capeMoveAnimData;
                width = 2f;
                height = 2f;
                offsetX = -1f;
                offsetY = -0.5f;
            }
        };
        rC.addTextureDatas(2, this.capeTextureData);

        ControllableComponent cC = new ControllableComponent(new PlayerInput());

        PhysicsComponent pC = new PhysicsComponent();
        pC.body = physicsBuilder.makeBody(
                tC.position.x, tC.position.y,
                BodyType.DynamicBody, 7.5f, true);
        pC.body.setFixedRotation(true);
        pC.fixtures.put(
                "Collider",
                physicsBuilder.addFixture(
                        pC.body, colliderShape, 1f,
                        Globals.PlayerCategory, Globals.ColPlayerMask, false));
        pC.fixtures.put(
                "HurtBox",
                physicsBuilder.addFixture(pC.body, hurtBoxShape,
                        Globals.PlayerCategory, Globals.HitboxCategory, true));

        makeWeapon(entity);

        entityManager.build(new PlayerComponent(), new MidfieldComponent(),
                rC, tC, cC, pC,
                new MovementComponent(new Vector2(0f, 0f), 10f),
                new HealthComponent());
        entityManager.finish();
    }

    private void makeWeapon(Entity entity) {
        weaponBuilder.begin(10, 10, 0.5f, Globals.EnemyCategory);

        Shape hitbox = ShapeBuilder.poly(
                new float[] { 0f, 0f, 0.5f, 0.5f, 1f, 0f, 1.5f, -1f });

        weaponBuilder.addHitbox(.1f, 0.3f, hitbox);

        FrameData frames = new FrameData(4, 0.1f, "AtkAnim");

        weaponBuilder.end(
                "weapons/BasicSword/BasicSword.atlas", frames, 0,
                3f, 3f, -0.9f, -0.5f, entity);
    }

    private FrameData[][] playerFrameDatas() {
        FrameData[][] frameDatas = new FrameData[3][];
        frameDatas[0] = new FrameData[] {
                new FrameData(10, 0.1f, "MoveLeft"),
                new FrameData(4, 0.2f, "MoveDown"),
                new FrameData(10, 0.1f, "MoveRight"),
                new FrameData(4, 0.2f, "MoveUp")
        };
        frameDatas[1] = new FrameData[] {
                new FrameData(10, 0.1f, "MoveLeft"),
                new FrameData(4, 0.2f, "MoveDown"),
                new FrameData(10, 0.1f, "MoveRight"),
                new FrameData(4, 0.2f, "MoveUp")
        };
        frameDatas[2] = new FrameData[] {
                new FrameData(10, 0.1f, "MoveLeft"),
                new FrameData(4, 0.2f, "MoveDown"),
                new FrameData(10, 0.1f, "MoveRight"),
                new FrameData(4, 0.2f, "MoveUp")
        };
        return frameDatas;
    }

    private FrameData[][] capeFrameDatas() {
        FrameData[][] frameDatas = new FrameData[3][];
        frameDatas[0] = new FrameData[] {
                new FrameData(10, 0.1f, "CapeMoveLeft"),
                new FrameData(4, 0.2f, "CapeMoveDown"),
                new FrameData(10, 0.1f, "CapeMoveRight"),
                new FrameData(4, 0.2f, "CapeMoveUp")
        };
        frameDatas[1] = new FrameData[] {
                new FrameData(10, 0.1f, "CapeMoveLeft"),
                new FrameData(4, 0.2f, "CapeMoveDown"),
                new FrameData(10, 0.1f, "CapeMoveRight"),
                new FrameData(4, 0.2f, "CapeMoveUp")
        };
        frameDatas[2] = new FrameData[] {
                new FrameData(10, 0.1f, "CapeMoveLeft"),
                new FrameData(4, 0.2f, "CapeMoveDown"),
                new FrameData(10, 0.1f, "CapeMoveRight"),
                new FrameData(4, 0.2f, "CapeMoveUp")
        };
        return frameDatas;
    }

    private class PlayerInput extends Inputhandler {
        @Override
        public void mouseEvent(MouseInputType type, int screenX, int screenY, int button) {
            if (type == MouseInputType.MOVED || type == MouseInputType.DRAGGED) {
                mouseMoved(screenX, screenY);
            } else if (type == MouseInputType.DOWN && button == Globals.AttackButton) {
                mousePressed();
            } else if (type == MouseInputType.UP && button == Globals.AttackButton) {
                mouseReleased();
            }
        }

        private void mouseMoved(int x, int y) {
            // Convert screen coordinates to world coordinates
            Vector3 worldCoords = camera.unproject(new Vector3(x, y, 0));
            apply(Mappers.weaponMapper, comp -> {
                Vector2 weaponPos = comp.body.getPosition();
                comp.target = new Vector2(worldCoords.x - weaponPos.x, worldCoords.y - weaponPos.y);
            });
        }

        private void mousePressed() {
            apply(Mappers.weaponMapper, comp -> {
                if (comp.state != WeaponStates.IDLE) {
                    return;
                }
                if (comp.chargetime != 0f) {
                    comp.time = 0f;
                    comp.state = WeaponStates.CHARGING;
                } else {
                    comp.state = WeaponStates.ATTACKING;
                }
            });
        }

        private void mouseReleased() {
            apply(Mappers.weaponMapper, comp -> {
                if (comp.state == WeaponStates.CHARGING) {
                    comp.state = WeaponStates.ATTACKING;
                }
            });
        }

        @Override
        public void pollevent() {
            movement();
            apply(Mappers.transformMapper, comp -> {
                camera.position.set(comp.position.x, comp.position.y, 0);
            });
            apply(Mappers.renderableMapper, this::animation);
        }

        private void movement() {
            Vector2 velocity = new Vector2(0, 0);
            if (Gdx.input.isKeyPressed(Globals.MoveUpKey)) {
                velocity.y += 1;
            }
            if (Gdx.input.isKeyPressed(Globals.MoveDownKey)) {
                velocity.y -= 1;
            }
            if (Gdx.input.isKeyPressed(Globals.MoveLeftKey)) {
                velocity.x -= 1;
            }
            if (Gdx.input.isKeyPressed(Globals.MoveRightKey)) {
                velocity.x += 1;
            }
            velocity.nor(); // Normalize the velocity vector
            apply(Mappers.movementMapper, comp -> {
                comp.velocity.set(velocity).scl(comp.max_speed);
            });
        }

        private void animation(RenderableComponent comp) {
            if (moveTextureData.animData.getName().equals("MoveUp")) {
                comp.changePriority(0, 2, capeTextureData);
            } else {
                comp.changePriority(2, 0, capeTextureData);
            }
        }
    }
}
