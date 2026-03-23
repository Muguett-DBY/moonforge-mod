package com.muguett.moonforge.render;

import com.muguett.moonforge.network.AkHitConfirmPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.sound.SoundEvents;

public final class HitFeedbackController {
    private static int hitmarkerTicks;
    private static boolean lethalHit;

    private HitFeedbackController() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(AkHitConfirmPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    hitmarkerTicks = 8;
                    lethalHit = payload.lethal();
                    if (context.client().player != null) {
                        float pitch = payload.lethal() ? 1.25F : 1.85F;
                        context.client().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 0.45F, pitch);
                    }
                }));
        HudRenderCallback.EVENT.register(HitFeedbackController::renderHud);
    }

    public static boolean hasActiveHitmarker() {
        return hitmarkerTicks > 0;
    }

    public static void tick() {
        if (hitmarkerTicks > 0) {
            hitmarkerTicks--;
        }
    }

    private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (hitmarkerTicks <= 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;
        int color = lethalHit ? 0xFFFF7070 : 0xFFF4F4F4;
        int size = lethalHit ? 8 : 6;
        int gap = 3;

        context.fill(centerX - size, centerY - size, centerX - gap, centerY - gap, color);
        context.fill(centerX + gap, centerY - size, centerX + size, centerY - gap, color);
        context.fill(centerX - size, centerY + gap, centerX - gap, centerY + size, color);
        context.fill(centerX + gap, centerY + gap, centerX + size, centerY + size, color);
    }
}
