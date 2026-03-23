package com.muguett.moonforge.entity;

import com.muguett.moonforge.item.ModItems;
import com.muguett.moonforge.network.AkHitConfirmPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class AkBulletEntity extends ThrownItemEntity {
    private float baseDamage = 8.0F;
    private double gravityScale = 0.0025D;
    private int trailStyle;

    public AkBulletEntity(EntityType<? extends AkBulletEntity> entityType, World world) {
        super(entityType, world);
    }

    public AkBulletEntity(EntityType<? extends AkBulletEntity> entityType, World world, LivingEntity owner) {
        super(entityType, owner, world, new ItemStack(ModItems.AK47_ROUND));
    }

    public void setBaseDamage(float baseDamage) {
        this.baseDamage = baseDamage;
    }

    public void setGravityScale(double gravityScale) {
        this.gravityScale = gravityScale;
    }

    public void setTrailStyle(int trailStyle) {
        this.trailStyle = trailStyle;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        ParticleEffect particle = switch (trailStyle) {
            case 1 -> ParticleTypes.CRIT;
            case 2 -> ParticleTypes.SMALL_FLAME;
            case 3 -> ParticleTypes.POOF;
            case 4 -> ParticleTypes.GLOW;
            default -> ParticleTypes.ELECTRIC_SPARK;
        };

        serverWorld.spawnParticles(particle, this.getX(), this.getY(), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.AK47_ROUND;
    }

    @Override
    protected double getGravity() {
        return gravityScale;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
            boolean hurt = entityHitResult.getEntity().damage(serverWorld, this.getDamageSources().mobProjectile(this, owner), baseDamage);
            if (hurt && owner instanceof ServerPlayerEntity serverPlayer) {
                boolean lethal = entityHitResult.getEntity() instanceof LivingEntity living && (living.getHealth() <= 0.0F || !living.isAlive());
                ServerPlayNetworking.send(serverPlayer, new AkHitConfirmPayload(lethal));
            }
        }
        this.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.4F, 1.3F);
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.playSound(SoundEvents.BLOCK_METAL_HIT, 0.35F, 1.35F);
        this.discard();
    }
}
