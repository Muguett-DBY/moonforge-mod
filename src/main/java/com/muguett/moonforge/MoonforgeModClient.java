package com.muguett.moonforge;

import com.muguett.moonforge.config.MoonforgeConfigManager;
import com.muguett.moonforge.entity.AkBulletEntity;
import com.muguett.moonforge.entity.ArsenalSpellEntity;
import com.muguett.moonforge.entity.GuidedArrowEntity;
import com.muguett.moonforge.entity.ModEntities;
import com.muguett.moonforge.entity.VoidburstBoltEntity;
import com.muguett.moonforge.render.GuidedCameraController;
import com.muguett.moonforge.render.TrajectoryRenderer;
import com.muguett.moonforge.render.WorldSafetyOverlay;
import com.muguett.moonforge.targeting.TargetLockManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class MoonforgeModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MoonforgeConfigManager.load();
        TargetLockManager.initialize();
        GuidedCameraController.initialize();
        EntityRendererRegistry.register(ModEntities.VOIDBURST_BOLT, FlyingItemEntityRenderer<VoidburstBoltEntity>::new);
        EntityRendererRegistry.register(ModEntities.GUIDED_ARROW, FlyingItemEntityRenderer<GuidedArrowEntity>::new);
        EntityRendererRegistry.register(ModEntities.ARSENAL_SPELL, FlyingItemEntityRenderer<ArsenalSpellEntity>::new);
        EntityRendererRegistry.register(ModEntities.AK47_BULLET, FlyingItemEntityRenderer<AkBulletEntity>::new);
        TrajectoryRenderer.initialize();
        WorldSafetyOverlay.initialize();
        MoonforgeMod.LOGGER.info("Moonforge client initialized with projectile training overlays.");
    }
}
