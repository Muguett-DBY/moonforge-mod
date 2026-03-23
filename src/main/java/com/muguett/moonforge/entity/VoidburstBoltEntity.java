package com.muguett.moonforge.entity;

import com.muguett.moonforge.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class VoidburstBoltEntity extends ThrownItemEntity {
    private double customGravity = 0.006D;

    public VoidburstBoltEntity(EntityType<? extends VoidburstBoltEntity> entityType, World world) {
        super(entityType, world);
    }

    public VoidburstBoltEntity(EntityType<? extends VoidburstBoltEntity> entityType, World world, LivingEntity owner) {
        super(entityType, owner, world, new ItemStack(ModItems.VOIDBURST_ARBALEST));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.VOIDBURST_ARBALEST;
    }

    public void setCustomGravity(double customGravity) {
        this.customGravity = customGravity;
    }

    @Override
    protected double getGravity() {
        return customGravity;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        explode(entityHitResult.getEntity());
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        explode(null);
    }

    private void explode(Entity directTarget) {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        serverWorld.createExplosion(this, this.getX(), this.getY(), this.getZ(), 4.0F, true, World.ExplosionSourceType.NONE);
        serverWorld.getOtherEntities(this, this.getBoundingBox().expand(4.0D), entity -> entity instanceof LivingEntity).forEach(entity -> {
            entity.setOnFireFor(5);
            DamageSource source = this.getDamageSources().explosion(this, this.getOwner());
            entity.damage(serverWorld, source, directTarget == entity ? 20.0F : 10.0F);
        });
        this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.2F, 0.9F);
        this.discard();
    }
}
