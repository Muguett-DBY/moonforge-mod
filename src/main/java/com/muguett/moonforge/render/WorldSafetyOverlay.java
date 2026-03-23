package com.muguett.moonforge.render;

import com.muguett.moonforge.config.MoonforgeConfig;
import com.muguett.moonforge.config.MoonforgeConfigManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public final class WorldSafetyOverlay {
    private static final Text WARNING = Text.literal("Moonforge assistive targeting is disabled outside singleplayer.");

    private WorldSafetyOverlay() {
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register(WorldSafetyOverlay::renderWarning);
    }

    private static void renderWarning(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        MoonforgeConfig config = MoonforgeConfigManager.getConfig();

        if (client.player == null || !config.singleplayerOnlySafeguard || client.world == null || client.getServer() != null) {
            return;
        }

        int x = 8;
        int y = 8;
        int width = client.textRenderer.getWidth(WARNING) + 10;
        int height = 14;

        drawContext.fill(x - 2, y - 2, x + width, y + height, 0xA0202020);
        drawContext.drawText(client.textRenderer, WARNING, x, y, 0xFFEF9A9A, false);
    }
}