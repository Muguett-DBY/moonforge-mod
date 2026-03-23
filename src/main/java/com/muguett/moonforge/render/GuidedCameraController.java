package com.muguett.moonforge.render;

import com.muguett.moonforge.entity.GuidedArrowEntity;
import com.muguett.moonforge.network.GuidedArrowControlPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class GuidedCameraController {
    private static int pendingAcquireTicks;
    private static GuidedArrowEntity activeProjectile;

    private GuidedCameraController() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(GuidedCameraController::tick);
    }

    public static void beginTrackingWindow() {
        pendingAcquireTicks = 30;
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            resetCamera(client);
            return;
        }

        if (activeProjectile != null) {
            if (!activeProjectile.isAlive() || activeProjectile.age > 200) {
                resetCamera(client);
            } else {
                client.setCameraEntity(activeProjectile);
                sendControlInput(activeProjectile, client);
            }
            return;
        }

        if (pendingAcquireTicks <= 0) {
            return;
        }

        pendingAcquireTicks--;
        GuidedArrowEntity best = client.world.getEntitiesByClass(
                        GuidedArrowEntity.class,
                        client.player.getBoundingBox().expand(128.0D),
                        projectile -> projectile.getOwner() == client.player && projectile.age < 20
                ).stream()
                .max((left, right) -> Integer.compare(left.age, right.age))
                .orElse(null);

        if (best != null) {
            activeProjectile = best;
            client.setCameraEntity(best);
            pendingAcquireTicks = 0;
        }
    }

    private static void sendControlInput(GuidedArrowEntity projectile, MinecraftClient client) {
        float turnInput = 0.0F;
        float liftInput = 0.0F;

        if (client.options.leftKey.isPressed()) {
            turnInput -= 1.0F;
        }
        if (client.options.rightKey.isPressed()) {
            turnInput += 1.0F;
        }
        if (client.options.forwardKey.isPressed()) {
            liftInput += 1.0F;
        }
        if (client.options.backKey.isPressed()) {
            liftInput -= 1.0F;
        }

        ClientPlayNetworking.send(new GuidedArrowControlPayload(projectile.getId(), turnInput, liftInput));
    }

    private static void resetCamera(MinecraftClient client) {
        pendingAcquireTicks = 0;
        activeProjectile = null;
        if (client.player != null) {
            client.setCameraEntity(client.player);
        }
    }
}
