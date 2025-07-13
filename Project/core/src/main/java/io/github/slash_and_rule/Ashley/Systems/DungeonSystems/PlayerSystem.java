package io.github.slash_and_rule.Ashley.Systems.DungeonSystems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import io.github.slash_and_rule.Globals;
import io.github.slash_and_rule.Animations.AnimData;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent;
import io.github.slash_and_rule.Ashley.Components.InactiveComponent;
import io.github.slash_and_rule.Ashley.Components.MovementComponent;
import io.github.slash_and_rule.Ashley.Components.PlayerComponent;
import io.github.slash_and_rule.Ashley.Components.TransformComponent;
import io.github.slash_and_rule.Ashley.Components.ControllableComponent.MouseData;
import io.github.slash_and_rule.Ashley.Components.DrawingComponents.AnimatedComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent;
import io.github.slash_and_rule.Ashley.Components.DungeonComponents.WeaponComponent.WeaponStates;
import io.github.slash_and_rule.Ashley.Systems.InputSystem.MouseInputType;
import io.github.slash_and_rule.Utils.Mappers;

public class PlayerSystem extends IteratingSystem {
    private Camera camera;

    public PlayerSystem(Camera camera, int priority) {
        super(Family.all(PlayerComponent.class, ControllableComponent.class,
                TransformComponent.class, WeaponComponent.class, MovementComponent.class, AnimatedComponent.class)
                .exclude(InactiveComponent.class).get(),
                priority); // Set the priority of this system, 0 is default
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        handlePlayerInput(entity, deltaTime);
    }

    private void handlePlayerInput(Entity entity, float deltaTime) {
        ControllableComponent controllable = Mappers.controllableMapper.get(entity);
        WeaponComponent weaponComp = Mappers.weaponMapper.get(entity);
        TransformComponent transComp = Mappers.transformMapper.get(entity);
        MovementComponent moveComp = Mappers.movementMapper.get(entity);
        AnimatedComponent animComp = Mappers.animatedMapper.get(entity);

        for (MouseData data : controllable.mouseQueue) {
            mouseEvent(data.type, data.screenX, data.screenY, data.button, transComp, weaponComp);
        }

        movement(moveComp);
        camera.position.set(transComp.position.x, transComp.position.y, 0);

        AnimData data = animComp.animations.get("CapeMove");
        if (data.getName().equals("CapeMoveUp")) {
            data.setPriority(2);
        } else {
            data.setPriority(0);
        }
    }

    public void mouseEvent(MouseInputType type, int screenX, int screenY, int button, TransformComponent transComp,
            WeaponComponent weaponComp) {
        if (type == MouseInputType.MOVED || type == MouseInputType.DRAGGED) {
            mouseMoved(screenX, screenY, transComp, weaponComp);
        } else if (type == MouseInputType.DOWN && button == Globals.AttackButton) {
            mousePressed(weaponComp);
        } else if (type == MouseInputType.UP && button == Globals.AttackButton) {
            mouseReleased(weaponComp);
        }
    }

    private void mouseMoved(int x, int y, TransformComponent transComp, WeaponComponent weaponComp) {
        // Convert screen coordinates to world coordinates
        Vector3 worldCoords = camera.unproject(new Vector3(x, y, 0));

        Vector2 pos = transComp.position;
        weaponComp.target.set(worldCoords.x - pos.x, worldCoords.y - pos.y);
    }

    private void mousePressed(WeaponComponent weaponComp) {
        if (weaponComp.state != WeaponStates.IDLE) {
            return;
        }
        if (weaponComp.chargetime != 0f) {
            weaponComp.time = 0f;
            weaponComp.state = WeaponStates.CHARGING;
        } else {
            weaponComp.state = WeaponStates.ATTACKING;
        }
    }

    private void mouseReleased(WeaponComponent weaponComp) {
        if (weaponComp.state == WeaponStates.CHARGING) {
            weaponComp.state = WeaponStates.ATTACKING;
        }
    }

    private void movement(MovementComponent moveComp) {
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
        moveComp.velocity.set(velocity).scl(moveComp.max_speed);
    }
}
