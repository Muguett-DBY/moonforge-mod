package com.muguett.moonforge.targeting;

import com.muguett.moonforge.config.MoonforgeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public final class TargetSelector {
    private TargetSelector() {
    }

    public static LivingEntity getLookedAtTarget(MinecraftClient client, MoonforgeConfig config) {
        if (client.player == null || client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return null;
        }

        Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
        if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isAlive()) {
            return null;
        }

        if (config.hostileTargetsOnly && !(livingEntity instanceof HostileEntity)) {
            return null;
        }

        double maxRange = config.maxTargetRange;
        if (client.player.squaredDistanceTo(livingEntity) > maxRange * maxRange) {
            return null;
        }

        return livingEntity;
    }
}
