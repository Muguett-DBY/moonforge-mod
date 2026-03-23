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
    private static final int INPUT_TIMEOUT_TICKS = 3;
    private double customGravity = 0.018D;
    private double steerStrength = 0.24D;
    private float turnInput;
    private float liftInput;
    private int inputTicksRemaining;

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

    public void setControlInput(float turnInput, float liftInput) {
        this.turnInput = MathHelper.clamp(turnInput, -1.0F, 1.0F);
        this.liftInput = MathHelper.clamp(liftInput, -1.0F, 1.0F);
        this.inputTicksRemaining = INPUT_TIMEOUT_TICKS;
    }

    @Override
    protected double getGravity() {
        return customGravity;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age > MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }

        steerFromControlInput();
        updateRotationFromVelocity();
    }

    private void steerFromControlInput() {
        Vec3d velocity = this.getVelocity();
        if (velocity.lengthSquared() < 1.0E-6D) {
            return;
        }

        if (inputTicksRemaining > 0) {
            inputTicksRemaining--;
        } else {
            turnInput = 0.0F;
            liftInput = 0.0F;
        }

        double speed = Math.max(0.8D, velocity.length());
        Vec3d forward = velocity.normalize();
        Vec3d worldUp = new Vec3d(0.0D, 1.0D, 0.0D);
        Vec3d right = worldUp.crossProduct(forward);
        if (right.lengthSquared() < 1.0E-6D) {
            right = new Vec3d(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        double yawRate = Math.max(0.008D, steerStrength * 0.12D);
        double pitchRate = Math.max(0.008D, steerStrength * 0.10D);

        Vec3d steered = rotateAroundAxis(forward, worldUp, -turnInput * yawRate);
        steered = rotateAroundAxis(steered, right, -liftInput * pitchRate);

        if (steered.lengthSquared() < 1.0E-6D) {
            return;
        }

        this.setVelocity(steered.normalize().multiply(speed));
    }

    private void updateRotationFromVelocity() {
        Vec3d velocity = this.getVelocity();
        if (velocity.lengthSquared() < 1.0E-6D) {
            return;
        }

        double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        float yaw = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(velocity.y, horizontal));
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.setBodyYaw(yaw);
        this.setHeadYaw(yaw);
    }

    private static Vec3d rotateAroundAxis(Vec3d vector, Vec3d axis, double angle) {
        if (Math.abs(angle) < 1.0E-6D || axis.lengthSquared() < 1.0E-6D) {
            return vector;
        }

        Vec3d normalizedAxis = axis.normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return vector.multiply(cos)
                .add(normalizedAxis.crossProduct(vector).multiply(sin))
                .add(normalizedAxis.multiply(normalizedAxis.dotProduct(vector) * (1.0D - cos)));
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
