package com.muguett.moonforge.item;

import com.muguett.moonforge.entity.AkBulletEntity;
import com.muguett.moonforge.entity.ModEntities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpyglassItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Ak47Item extends SpyglassItem {
    private static final int MAGAZINE_SIZE = 30;
    private static final int DAMAGE_CAP = MAGAZINE_SIZE + 1;
    private static final int FIRE_INTERVAL_TICKS = 2;
    private static final int RELOAD_TICKS = 24;

    public Ak47Item(Settings settings) {
        super(settings.maxCount(1).maxDamage(DAMAGE_CAP));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (getAmmo(stack) <= 0 || (user.isSneaking() && getAmmo(stack) < MAGAZINE_SIZE)) {
            reload(world, user, stack);
            return ActionResult.CONSUME;
        }

        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player) || world.isClient()) {
            return;
        }

        int elapsed = this.getMaxUseTime(stack, user) - remainingUseTicks;
        if (elapsed <= 0 || elapsed % FIRE_INTERVAL_TICKS != 1) {
            return;
        }

        if (getAmmo(stack) <= 0) {
            player.stopUsingItem();
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.45F, 1.6F);
            return;
        }

        fireRound((ServerWorld) world, player, stack);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F * getAmmo(stack) / MAGAZINE_SIZE);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xC69C3A;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    private void fireRound(ServerWorld world, PlayerEntity player, ItemStack stack) {
        AkBulletEntity bullet = new AkBulletEntity(ModEntities.AK47_BULLET, world, player);
        bullet.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 5.4F, 0.85F);
        world.spawnEntity(bullet);

        stack.setDamage(Math.min(MAGAZINE_SIZE, stack.getDamage() + 1));
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 0.55F, 0.72F);
        Vec3d muzzle = player.getEyePos().add(player.getRotationVec(1.0F).multiply(0.8D));
        world.spawnParticles(net.minecraft.particle.ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 3, 0.03D, 0.03D, 0.03D, 0.005D);
    }

    private void reload(World world, PlayerEntity user, ItemStack stack) {
        if (getAmmo(stack) >= MAGAZINE_SIZE) {
            return;
        }

        stack.setDamage(0);
        user.getItemCooldownManager().set(stack, RELOAD_TICKS);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 0.8F, 0.85F);
    }

    private static int getAmmo(ItemStack stack) {
        return Math.max(0, MAGAZINE_SIZE - stack.getDamage());
    }
}
