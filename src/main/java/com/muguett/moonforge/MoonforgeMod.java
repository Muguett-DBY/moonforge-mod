package com.muguett.moonforge;

import com.muguett.moonforge.config.MoonforgeConfigManager;
import com.muguett.moonforge.entity.GuidedArrowEntity;
import com.muguett.moonforge.entity.ModEntities;
import com.muguett.moonforge.item.ModItemGroups;
import com.muguett.moonforge.item.ModItems;
import com.muguett.moonforge.network.GuidedArrowControlPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoonforgeMod implements ModInitializer {
    public static final String MOD_ID = "moonforge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        MoonforgeConfigManager.load();
        PayloadTypeRegistry.playC2S().register(GuidedArrowControlPayload.ID, GuidedArrowControlPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(GuidedArrowControlPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    if (!(context.player().getEntityWorld().getEntityById(payload.projectileId()) instanceof GuidedArrowEntity guidedArrow)) {
                        return;
                    }
                    if (guidedArrow.getOwner() != context.player()) {
                        return;
                    }
                    guidedArrow.setControlInput(payload.turnInput(), payload.liftInput());
                }));
        ModEntities.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();
        LOGGER.info("Moonforge initialized in learning mode.");
    }
}
