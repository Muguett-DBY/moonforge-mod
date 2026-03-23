package com.muguett.moonforge.render;

import com.muguett.moonforge.entity.GuidedArrowEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

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
                Vec3d velocity = activeProjectile.getVelocity();
                if (velocity.lengthSquared() > 1.0E-6D) {
                    client.player.setYaw(activeProjectile.getYaw());
                    client.player.setPitch(activeProjectile.getPitch());
                }
                client.setCameraEntity(activeProjectile);
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
            client.player.setYaw(best.getYaw());
            client.player.setPitch(best.getPitch());
            client.setCameraEntity(best);
            pendingAcquireTicks = 0;
        }
    }

    private static void resetCamera(MinecraftClient client) {
        pendingAcquireTicks = 0;
        activeProjectile = null;
        if (client.player != null) {
            client.setCameraEntity(client.player);
        }
    }
}
