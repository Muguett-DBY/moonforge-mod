package com.muguett.moonforge.render;

import com.muguett.moonforge.config.MoonforgeConfig;
import com.muguett.moonforge.config.MoonforgeConfigManager;
import com.muguett.moonforge.physics.ProjectilePhysicsProfile;
import com.muguett.moonforge.physics.TrajectoryPoint;
import com.muguett.moonforge.physics.TrajectoryResult;
import com.muguett.moonforge.physics.TrajectorySimulator;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public final class TrajectoryRenderer {
    private static final int ARC_R = 64;
    private static final int ARC_G = 219;
    private static final int ARC_B = 145;
    private static final int IMPACT_R = 255;
    private static final int IMPACT_G = 94;
    private static final int IMPACT_B = 91;

    private TrajectoryRenderer() {
    }

    public static void initialize() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(TrajectoryRenderer::renderTrajectory);
    }

    private static void renderTrajectory(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || client.world == null || context.matrices() == null || context.consumers() == null) {
            return;
        }

        MoonforgeConfig config = MoonforgeConfigManager.getConfig();
        if (!config.trajectoryEnabled) {
            return;
        }

        if (config.singleplayerOnlySafeguard && client.getServer() == null) {
            return;
        }

        AimPreview preview = getActivePreview(player, config);
        if (preview == null) {
            return;
        }

        TrajectoryResult result = TrajectorySimulator.simulate(
                client.world,
                player,
                preview.startPosition(),
                preview.velocity(),
                preview.profile()
        );

        MatrixStack matrices = context.matrices();
        Vec3d camera = player.getCameraPosVec(1.0F);
        VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayers.lines());

        drawArc(matrices, vertexConsumer, result.points(), camera);
        drawImpactMarker(matrices, vertexConsumer, result.impactPosition(), result.impactType(), camera);
    }

    private static void drawArc(MatrixStack matrices, VertexConsumer vertexConsumer, List<TrajectoryPoint> points, Vec3d camera) {
        MatrixStack.Entry entry = matrices.peek();

        for (int index = 0; index < points.size() - 1; index++) {
            Vec3d current = points.get(index).position().subtract(camera);
            Vec3d next = points.get(index + 1).position().subtract(camera);
            Vec3d delta = next.subtract(current);
            Vec3d normal = delta.lengthSquared() > 0.0D ? delta.normalize() : new Vec3d(0.0D, 1.0D, 0.0D);

            vertexConsumer.vertex(entry, (float) current.x, (float) current.y, (float) current.z)
                    .color(ARC_R, ARC_G, ARC_B, 255)
                    .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
            vertexConsumer.vertex(entry, (float) next.x, (float) next.y, (float) next.z)
                    .color(ARC_R, ARC_G, ARC_B, 255)
                    .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
        }
    }

    private static void drawImpactMarker(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d impactPosition,
                                         HitResult.Type impactType, Vec3d camera) {
        Vec3d center = impactPosition.subtract(camera);
        double size = impactType == HitResult.Type.MISS ? 0.12D : 0.24D;
        int alpha = impactType == HitResult.Type.MISS ? 80 : 220;

        drawLine(matrices, vertexConsumer, center.add(-size, 0.0D, 0.0D), center.add(size, 0.0D, 0.0D), alpha);
        drawLine(matrices, vertexConsumer, center.add(0.0D, -size, 0.0D), center.add(0.0D, size, 0.0D), alpha);
        drawLine(matrices, vertexConsumer, center.add(0.0D, 0.0D, -size), center.add(0.0D, 0.0D, size), alpha);
    }

    private static void drawLine(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d start, Vec3d end, int alpha) {
        MatrixStack.Entry entry = matrices.peek();
        Vec3d delta = end.subtract(start);
        Vec3d normal = delta.lengthSquared() > 0.0D ? delta.normalize() : new Vec3d(0.0D, 1.0D, 0.0D);

        vertexConsumer.vertex(entry, (float) start.x, (float) start.y, (float) start.z)
                .color(IMPACT_R, IMPACT_G, IMPACT_B, alpha)
                .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
        vertexConsumer.vertex(entry, (float) end.x, (float) end.y, (float) end.z)
                .color(IMPACT_R, IMPACT_G, IMPACT_B, alpha)
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
