package com.muguett.moonforge.entity;

import com.muguett.moonforge.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class AkBulletEntity extends ThrownItemEntity {
    public AkBulletEntity(EntityType<? extends AkBulletEntity> entityType, World world) {
        super(entityType, world);
    }

    public AkBulletEntity(EntityType<? extends AkBulletEntity> entityType, World world, LivingEntity owner) {
        super(entityType, owner, world, new ItemStack(ModItems.AK47_ROUND));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.AK47_ROUND;
    }

    @Override
    protected double getGravity() {
        return 0.0025D;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
            entityHitResult.getEntity().damage(serverWorld, this.getDamageSources().mobProjectile(this, owner), 8.0F);
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
