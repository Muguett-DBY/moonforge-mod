package com.muguett.moonforge.entity;

import com.muguett.moonforge.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GuidedArrowEntity extends ThrownItemEntity {
    private static final int MAX_LIFETIME_TICKS = 200;
    private double customGravity = 0.018D;
    private double steerStrength = 0.24D;
    private float previousVisualYaw;
    private float previousVisualPitch;

    public GuidedArrowEntity(EntityType<? extends GuidedArrowEntity> entityType, World world) {
        super(entityType, world);
    }

    public GuidedArrowEntity(EntityType<? extends GuidedArrowEntity> entityType, World world, LivingEntity owner) {
        super(entityType, owner, world, new ItemStack(ModItems.VERDANT_GUIDEBOW));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.VERDANT_GUIDEBOW;
    }

    public void setCustomGravity(double customGravity) {
        this.customGravity = customGravity;
    }

    public void setSteerStrength(double steerStrength) {
        this.steerStrength = steerStrength;
    }

    @Override
    protected double getGravity() {
        return customGravity;
    }

    @Override
    public void tick() {
        previousVisualYaw = this.getYaw();
        previousVisualPitch = this.getPitch();
        super.tick();

        if (this.age > MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }

        if (this.getOwner() instanceof LivingEntity owner) {
            Vec3d velocity = this.getVelocity();
            Vec3d desiredDirection = owner.getRotationVec(1.0F).normalize();
            double speed = Math.max(0.8D, velocity.length());
            Vec3d steeredVelocity = velocity.multiply(1.0D - steerStrength).add(desiredDirection.multiply(speed * steerStrength));
            Vec3d normalized = steeredVelocity.normalize().multiply(speed);
            this.setVelocity(normalized);
        }

        updateVisualRotationFromVelocity();
    }

    private void updateVisualRotationFromVelocity() {
        Vec3d velocity = this.getVelocity();
        if (velocity.lengthSquared() < 1.0E-6D) {
            return;
        }

        float targetYaw = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z));
        float targetPitch = (float) Math.toDegrees(Math.atan2(velocity.y, Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)));
        float smoothedYaw = previousVisualYaw + MathHelper.wrapDegrees(targetYaw - previousVisualYaw) * 0.5F;
        float smoothedPitch = previousVisualPitch + (targetPitch - previousVisualPitch) * 0.5F;
        this.setYaw(smoothedYaw);
        this.setPitch(smoothedPitch);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
            entityHitResult.getEntity().damage(serverWorld, this.getDamageSources().mobProjectile(this, owner), 50.0F);
        }
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.discard();
    }
}
