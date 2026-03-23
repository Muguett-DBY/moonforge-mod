package com.muguett.moonforge;

import com.muguett.moonforge.config.MoonforgeConfigManager;
import com.muguett.moonforge.entity.ModEntities;
import com.muguett.moonforge.item.ModItemGroups;
import com.muguett.moonforge.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoonforgeMod implements ModInitializer {
    public static final String MOD_ID = "moonforge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        MoonforgeConfigManager.load();
        ModEntities.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();
        LOGGER.info("Moonforge initialized in learning mode.");
    }
}
