package com.muguett.moonforge;

import com.muguett.moonforge.config.MoonforgeConfigManager;
import com.muguett.moonforge.render.TrajectoryRenderer;
import com.muguett.moonforge.render.WorldSafetyOverlay;
import com.muguett.moonforge.targeting.TargetLockManager;
import net.fabricmc.api.ClientModInitializer;

public class MoonforgeModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MoonforgeConfigManager.load();
        TargetLockManager.initialize();
        TrajectoryRenderer.initialize();
        WorldSafetyOverlay.initialize();
        MoonforgeMod.LOGGER.info("Moonforge client initialized with projectile training overlays.");
    }
}
