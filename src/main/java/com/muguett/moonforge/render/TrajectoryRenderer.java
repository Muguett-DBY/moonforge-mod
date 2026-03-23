package com.muguett.moonforge.render;

import com.muguett.moonforge.config.MoonforgeConfig;
import com.muguett.moonforge.config.MoonforgeConfigManager;
import com.muguett.moonforge.physics.ProjectilePhysicsProfile;
import com.muguett.moonforge.physics.TrajectoryPoint;
import com.muguett.moonforge.physics.TrajectoryResult;
import com.muguett.moonforge.physics.TrajectorySimulator;
import com.muguett.moonforge.prediction.AimRecommendation;
import com.muguett.moonforge.prediction.AimSolver;
import com.muguett.moonforge.targeting.TargetLockManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Locale;

public final class TrajectoryRenderer {
    private static final int ARC_R = 64;
    private static final int ARC_G = 219;
    private static final int ARC_B = 145;
    private static final int IMPACT_R = 255;
    private static final int IMPACT_G = 94;
    private static final int IMPACT_B = 91;
    private static final int TARGET_R = 255;
    private static final int TARGET_G = 214;
    private static final int TARGET_B = 10;
    private static final float ARC_LINE_WIDTH = 2.0F;
    private static final float IMPACT_LINE_WIDTH = 2.5F;
    private static final float TARGET_LINE_WIDTH = 3.0F;

    private static AimRecommendation lastRecommendation;
    private static LivingEntity lastTarget;

    private TrajectoryRenderer() {
    }

    public static void initialize() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(TrajectoryRenderer::renderTrajectory);
        HudRenderCallback.EVENT.register(TrajectoryRenderer::renderHud);
    }

    private static void renderTrajectory(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        lastRecommendation = null;
        lastTarget = null;

        if (player == null || client.world == null || context.matrices() == null || context.consumers() == null) {
            return;
        }

        MoonforgeConfig config = MoonforgeConfigManager.getConfig();
        if (config.singleplayerOnlySafeguard && client.getServer() == null) {
            return;
        }

        AimPreview preview = getActivePreview(player, config);
        if (preview == null) {
            return;
        }

        MatrixStack matrices = context.matrices();
        Vec3d camera = player.getCameraPosVec(1.0F);
        VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayers.lines());

        if (config.trajectoryEnabled) {
            TrajectoryResult result = TrajectorySimulator.simulate(
                    client.world,
                    player,
                    preview.startPosition(),
                    preview.velocity(),
                    preview.profile()
            );

            drawArc(matrices, vertexConsumer, result.points(), camera, ARC_R, ARC_G, ARC_B, ARC_LINE_WIDTH);
            drawImpactMarker(matrices, vertexConsumer, result.impactPosition(), result.impactType(), camera);
        }

        LivingEntity lockedTarget = TargetLockManager.getLockedTarget();
        if (!config.targetIndicatorEnabled || lockedTarget == null || !lockedTarget.isAlive()) {
            return;
        }

        double maxRange = config.maxTargetRange;
        if (player.squaredDistanceTo(lockedTarget) > maxRange * maxRange) {
            return;
        }

        Vec3d shooterPos = preview.startPosition();
        Vec3d currentAimDirection = player.getRotationVec(1.0F).normalize();
        Vec3d targetCenter = lockedTarget.getBoundingBox().getCenter().add(0.0D, lockedTarget.getHeight() * 0.15D, 0.0D);
        Vec3d targetVelocity = lockedTarget.getVelocity();
        AimRecommendation recommendation = AimSolver.solve(
                shooterPos,
                currentAimDirection,
                targetCenter,
                targetVelocity,
                preview.profile(),
                config.motionPredictionEnabled,
                config.predictionMultiplier
        );

        lastRecommendation = recommendation;
        lastTarget = lockedTarget;

        Vec3d predictedPoint = recommendation.predictedTargetPosition();
        drawLockMarker(matrices, vertexConsumer, predictedPoint, camera);

        Vec3d recommendedEnd = shooterPos.add(recommendation.recommendedAimDirection().multiply(3.0D));
        drawLine(matrices, vertexConsumer, shooterPos.subtract(camera), recommendedEnd.subtract(camera), 220, TARGET_R, TARGET_G, TARGET_B, TARGET_LINE_WIDTH);
    }

    private static void renderHud(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        MoonforgeConfig config = MoonforgeConfigManager.getConfig();

        if (client.player == null || client.world == null || lastRecommendation == null || lastTarget == null) {
            return;
        }

        if (config.singleplayerOnlySafeguard && client.getServer() == null) {
            return;
        }

        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;
        int boxWidth = 184;
        int boxHeight = 38;
        int x = centerX - boxWidth / 2;
        int y = centerY + 20;

        drawContext.fill(x, y, x + boxWidth, y + boxHeight, 0xA0101010);
        drawContext.fill(x, y, x + boxWidth, y + 1, 0x80FFD60A);
        drawContext.fill(x, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0x80FFD60A);
        drawContext.fill(x, y, x + 1, y + boxHeight, 0x80FFD60A);
        drawContext.fill(x + boxWidth - 1, y, x + boxWidth, y + boxHeight, 0x80FFD60A);
        drawContext.drawText(client.textRenderer, "Locked: " + lastTarget.getName().getString(), x + 6, y + 6, 0xFFFFD60A, false);
        drawContext.drawText(client.textRenderer,
                String.format(Locale.ROOT, "Yaw %.1f  Pitch %.1f", lastRecommendation.horizontalErrorDegrees(), lastRecommendation.verticalErrorDegrees()),
                x + 6, y + 17, 0xFFE0E0E0, false);
        drawContext.drawText(client.textRenderer,
                String.format(Locale.ROOT, "Flight %.1f ticks", lastRecommendation.estimatedFlightTicks()),
                x + 6, y + 28, 0xFFA0EACD, false);

        int indicatorX = centerX + MathHelper.clamp((int) (lastRecommendation.horizontalErrorDegrees() * 3.0D), -90, 90);
        int indicatorY = centerY + MathHelper.clamp((int) (lastRecommendation.verticalErrorDegrees() * 3.0D), -60, 60);
        drawContext.fill(indicatorX - 3, indicatorY - 3, indicatorX + 3, indicatorY + 3, 0xFFFFD60A);
    }

    private static void drawArc(MatrixStack matrices, VertexConsumer vertexConsumer, List<TrajectoryPoint> points, Vec3d camera,
                                int red, int green, int blue, float lineWidth) {
        for (int index = 0; index < points.size() - 1; index++) {
            Vec3d current = points.get(index).position().subtract(camera);
            Vec3d next = points.get(index + 1).position().subtract(camera);
            drawLine(matrices, vertexConsumer, current, next, 255, red, green, blue, lineWidth);
        }
    }

    private static void drawImpactMarker(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d impactPosition,
                                         HitResult.Type impactType, Vec3d camera) {
        Vec3d center = impactPosition.subtract(camera);
        double size = impactType == HitResult.Type.MISS ? 0.12D : 0.24D;
        int alpha = impactType == HitResult.Type.MISS ? 80 : 220;

        drawLine(matrices, vertexConsumer, center.add(-size, 0.0D, 0.0D), center.add(size, 0.0D, 0.0D), alpha, IMPACT_R, IMPACT_G, IMPACT_B, IMPACT_LINE_WIDTH);
        drawLine(matrices, vertexConsumer, center.add(0.0D, -size, 0.0D), center.add(0.0D, size, 0.0D), alpha, IMPACT_R, IMPACT_G, IMPACT_B, IMPACT_LINE_WIDTH);
        drawLine(matrices, vertexConsumer, center.add(0.0D, 0.0D, -size), center.add(0.0D, 0.0D, size), alpha, IMPACT_R, IMPACT_G, IMPACT_B, IMPACT_LINE_WIDTH);
    }

    private static void drawLockMarker(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d worldPosition, Vec3d camera) {
        Vec3d center = worldPosition.subtract(camera);
        double size = 0.25D;
        drawLine(matrices, vertexConsumer, center.add(-size, 0.0D, 0.0D), center.add(size, 0.0D, 0.0D), 220, TARGET_R, TARGET_G, TARGET_B, TARGET_LINE_WIDTH);
        drawLine(matrices, vertexConsumer, center.add(0.0D, -size, 0.0D), center.add(0.0D, size, 0.0D), 220, TARGET_R, TARGET_G, TARGET_B, TARGET_LINE_WIDTH);
        drawLine(matrices, vertexConsumer, center.add(0.0D, 0.0D, -size), center.add(0.0D, 0.0D, size), 220, TARGET_R, TARGET_G, TARGET_B, TARGET_LINE_WIDTH);
    }

    private static void drawLine(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d start, Vec3d end, int alpha,
                                 int red, int green, int blue, float lineWidth) {
        MatrixStack.Entry entry = matrices.peek();
        Vec3d delta = end.subtract(start);
        Vec3d normal = delta.lengthSquared() > 0.0D ? delta.normalize() : new Vec3d(0.0D, 1.0D, 0.0D);

        vertexConsumer.vertex(entry, (float) start.x, (float) start.y, (float) start.z)
                .color(red, green, blue, alpha)
                .lineWidth(lineWidth)
                .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
        vertexConsumer.vertex(entry, (float) end.x, (float) end.y, (float) end.z)
                .color(red, green, blue, alpha)
                .lineWidth(lineWidth)
                .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    private static AimPreview getActivePreview(ClientPlayerEntity player, MoonforgeConfig config) {
        ItemStack mainHand = player.getMainHandStack();
        AimPreview mainPreview = createPreview(player, Hand.MAIN_HAND, mainHand, config);
        if (mainPreview != null) {
            return mainPreview;
        }

        ItemStack offHand = player.getOffHandStack();
        return createPreview(player, Hand.OFF_HAND, offHand, config);
    }

    private static AimPreview createPreview(ClientPlayerEntity player, Hand hand, ItemStack stack, MoonforgeConfig config) {
        if (stack.isEmpty()) {
            return null;
        }

        Vec3d direction = player.getRotationVec(1.0F).normalize();
        Vec3d start = player.getCameraPosVec(1.0F).add(direction.multiply(0.2D));

        if (stack.isOf(Items.BOW) && config.bowEnabled && player.isUsingItem() && player.getActiveHand() == hand) {
            int useTicks = stack.getItem().getMaxUseTime(stack, player) - player.getItemUseTimeLeft();
            float pullProgress = BowItem.getPullProgress(useTicks);
            if (pullProgress < 0.1F) {
                return null;
            }

            double speed = pullProgress * 3.0D;
            return new AimPreview(ProjectilePhysicsProfile.bow(speed), start, direction.multiply(speed));
        }

        if (stack.isOf(Items.CROSSBOW) && config.crossbowEnabled) {
            if (!CrossbowItem.isCharged(stack)) {
                return null;
            }

            return new AimPreview(ProjectilePhysicsProfile.crossbow(), start, direction.multiply(3.15D));
        }

        if (stack.isOf(Items.TRIDENT) && config.tridentEnabled && player.isUsingItem() && player.getActiveHand() == hand) {
            int useTicks = stack.getItem().getMaxUseTime(stack, player) - player.getItemUseTimeLeft();
            if (useTicks < 10) {
                return null;
            }

            return new AimPreview(ProjectilePhysicsProfile.trident(), start, direction.multiply(2.5D));
        }

        return null;
    }

    private record AimPreview(ProjectilePhysicsProfile profile, Vec3d startPosition, Vec3d velocity) {
    }
}
