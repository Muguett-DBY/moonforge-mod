package com.muguett.moonforge.render;

import com.muguett.moonforge.item.FireMode;
import com.muguett.moonforge.item.FirearmItem;
import com.muguett.moonforge.item.FirearmStats;
import com.muguett.moonforge.network.AkFirePayload;
import com.muguett.moonforge.network.AkReloadPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public final class AkScopeController {
    private static Integer originalFov;
    private static boolean scoped;
    private static int clientFireCooldown;
    private static boolean previousUsePressed;
    private static boolean previousAttackPressed;

    private AkScopeController() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(AkScopeController::tick);
        HudRenderCallback.EVENT.register(AkScopeController::renderHud);
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.options == null) {
            restoreFov(client);
            previousUsePressed = false;
            previousAttackPressed = false;
            return;
        }

        Hand firearmHand = getHeldFirearmHand(client);
        if (firearmHand == null) {
            scoped = false;
            restoreFov(client);
            previousUsePressed = false;
            previousAttackPressed = false;
            return;
        }

        boolean usePressed = client.options.useKey.isPressed();
        if (usePressed && !previousUsePressed) {
            if (client.player.isSneaking()) {
                ClientPlayNetworking.send(new AkReloadPayload(firearmHand));
                scoped = false;
            } else {
                scoped = !scoped;
            }
        }
        previousUsePressed = usePressed;

        if (clientFireCooldown > 0) {
            clientFireCooldown--;
        }

        ItemStack stack = firearmHand == Hand.MAIN_HAND ? client.player.getMainHandStack() : client.player.getOffHandStack();
        FirearmStats stats = FirearmItem.getStats(stack);
        boolean attackPressed = client.options.attackKey.isPressed();
        boolean shouldFire = false;
        if (stats != null && clientFireCooldown <= 0) {
            shouldFire = switch (stats.fireMode()) {
                case AUTO -> attackPressed;
                case SEMI -> attackPressed && !previousAttackPressed;
            };
        }

        if (stats != null && shouldFire) {
            ClientPlayNetworking.send(new AkFirePayload(firearmHand, scoped));
            applyClientRecoil(client, stats, scoped);
            clientFireCooldown = stats.fireIntervalTicks();
        }
        previousAttackPressed = attackPressed;

        if (scoped) {
            applyScope(client, stats);
        } else {
            restoreFov(client);
        }
    }

    private static void applyClientRecoil(MinecraftClient client, FirearmStats stats, boolean scopedShot) {
        if (client.player == null || stats == null) {
            return;
        }
        float pitchKick = scopedShot ? stats.scopedPitchKick() : stats.hipPitchKick();
        float yawKick = scopedShot ? stats.scopedYawKick() : stats.hipYawKick();
        float side = client.player.getRandom().nextBoolean() ? 1.0F : -1.0F;
        client.player.setPitch(client.player.getPitch() - pitchKick);
        client.player.setYaw(client.player.getYaw() + side * yawKick);
    }

    private static Hand getHeldFirearmHand(MinecraftClient client) {
        if (client.player == null) {
            return null;
        }
        if (FirearmItem.getStats(client.player.getMainHandStack()) != null) {
            return Hand.MAIN_HAND;
        }
        if (FirearmItem.getStats(client.player.getOffHandStack()) != null) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    private static void applyScope(MinecraftClient client, FirearmStats stats) {
        if (stats == null) {
            return;
        }
        int currentFov = client.options.getFov().getValue();
        if (originalFov == null) {
            originalFov = currentFov;
        }

        int scopedFov = Math.max(12, (int) Math.round(originalFov / (double) stats.scopeDivisor()));
        if (currentFov != scopedFov) {
            client.options.getFov().setValue(scopedFov);
        }
    }

    private static void restoreFov(MinecraftClient client) {
        if (client.options == null) {
            originalFov = null;
            return;
        }
        if (originalFov != null && client.options.getFov().getValue() != originalFov) {
            client.options.getFov().setValue(originalFov);
        }
        originalFov = null;
    }

    private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        Hand hand = getHeldFirearmHand(client);
        if (client.player == null || hand == null) {
            return;
        }

        ItemStack stack = hand == Hand.MAIN_HAND ? client.player.getMainHandStack() : client.player.getOffHandStack();
        FirearmStats stats = FirearmItem.getStats(stack);
        if (stats == null) {
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        float reloadProgress = client.player.getItemCooldownManager().getCooldownProgress(stack, 0.0F);
        boolean reloading = reloadProgress > 0.0F && FirearmItem.getAmmo(stack) < stats.magazineSize();
        String status = reloading ? "RELOADING" : stats.fireMode().label();

        if (scoped) {
            int radius = Math.min(width, height) / 3;
            context.fill(0, 0, width, Math.max(0, centerY - radius), 0xD8000000);
            context.fill(0, Math.min(height, centerY + radius), width, height, 0xD8000000);
            context.fill(0, Math.max(0, centerY - radius), Math.max(0, centerX - radius), Math.min(height, centerY + radius), 0xD8000000);
            context.fill(Math.min(width, centerX + radius), Math.max(0, centerY - radius), width, Math.min(height, centerY + radius), 0xD8000000);

            context.fill(centerX - 1, centerY - radius, centerX + 1, centerY + radius, 0x70FF2E2E);
            context.fill(centerX - radius, centerY - 1, centerX + radius, centerY + 1, 0x70FF2E2E);
            context.fill(centerX - 12, centerY - 1, centerX - 3, centerY + 1, 0xC0FFD76A);
            context.fill(centerX + 3, centerY - 1, centerX + 12, centerY + 1, 0xC0FFD76A);
            context.fill(centerX - 1, centerY - 12, centerX + 1, centerY - 3, 0xC0FFD76A);
            context.fill(centerX - 1, centerY + 3, centerX + 1, centerY + 12, 0xC0FFD76A);
            context.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xE0FFD76A);

            context.drawText(client.textRenderer, stats.scopeLabel(), centerX + radius - 26, centerY - radius + 8, 0xFFD6C07A, false);
            context.drawText(client.textRenderer, Text.literal("Ammo " + FirearmItem.getAmmo(stack) + "/" + stats.magazineSize()), centerX - 28, centerY + radius - 16, 0xFFE6E6E6, false);
            context.drawText(client.textRenderer, Text.literal(status), centerX - 12, centerY + radius - 30, reloading ? 0xFFFF8A65 : 0xFFB8E986, false);
            renderReloadBar(context, centerX - 40, centerY + radius - 8, 80, reloadProgress, reloading);
        } else {
            context.drawText(client.textRenderer, Text.literal("Ammo " + FirearmItem.getAmmo(stack) + "/" + stats.magazineSize()), centerX - 28, height - 42, 0xFFE6E6E6, false);
            context.drawText(client.textRenderer, Text.literal(status), centerX - 18, height - 54, reloading ? 0xFFFF8A65 : 0xFFB8E986, false);
            renderReloadBar(context, centerX - 40, height - 18, 80, reloadProgress, reloading);
        }
    }

    private static void renderReloadBar(DrawContext context, int x, int y, int width, float progress, boolean visible) {
        if (!visible) {
            return;
        }
        context.fill(x, y, x + width, y + 4, 0x70000000);
        int filled = Math.max(0, Math.min(width, Math.round(width * progress)));
        context.fill(x, y, x + filled, y + 4, 0xFFFF8A65);
    }
}
