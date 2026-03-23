package com.muguett.moonforge.targeting;

import com.muguett.moonforge.MoonforgeMod;
import com.muguett.moonforge.config.MoonforgeConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class TargetLockManager {
    private static final KeyBinding LOCK_TARGET_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.moonforge.lock_target",
            InputUtil.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_4,
            KeyBinding.Category.MISC
    ));

    private static LivingEntity lockedTarget;

    private TargetLockManager() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(TargetLockManager::tick);
    }

    public static LivingEntity getLockedTarget() {
        return lockedTarget;
    }

    private static void tick(MinecraftClient client) {
        while (LOCK_TARGET_KEY.wasPressed()) {
            toggleTarget(client);
        }

        if (lockedTarget != null && (!lockedTarget.isAlive() || client.player == null || client.world == null)) {
            lockedTarget = null;
        }
    }

    private static void toggleTarget(MinecraftClient client) {
        MoonforgeConfig config = com.muguett.moonforge.config.MoonforgeConfigManager.getConfig();

        if (client.player == null || client.world == null) {
            return;
        }

        if (config.singleplayerOnlySafeguard && client.getServer() == null) {
            client.player.sendMessage(Text.literal("Moonforge target lock is disabled outside singleplayer."), true);
            lockedTarget = null;
            return;
        }

        LivingEntity candidate = TargetSelector.getLookedAtTarget(client, config);
        if (candidate == null) {
            lockedTarget = null;
            client.player.sendMessage(Text.literal("Moonforge cleared the current target."), true);
            return;
        }

        if (candidate == lockedTarget) {
            lockedTarget = null;
            client.player.sendMessage(Text.literal("Moonforge unlocked the target."), true);
            return;
        }

        lockedTarget = candidate;
        client.player.sendMessage(Text.literal("Moonforge locked target: " + candidate.getName().getString()), true);
        MoonforgeMod.LOGGER.info("Locked singleplayer practice target: {}", candidate.getName().getString());
    }
}
