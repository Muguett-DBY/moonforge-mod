package com.muguett.moonforge;

import net.fabricmc.api.ClientModInitializer;

public class MoonforgeModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MoonforgeMod.LOGGER.info("Moonforge client initialized.");
    }
}
