package com.muguett.moonforge.item;

import com.muguett.moonforge.entity.AkBulletEntity;
import com.muguett.moonforge.entity.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Ak47Item extends Item {
    public static final int MAGAZINE_SIZE = 30;
    private static final int DAMAGE_CAP = MAGAZINE_SIZE + 1;
    private static final int FIRE_INTERVAL_TICKS = 2;
    private static final int RELOAD_TICKS = 24;

    public Ak47Item(Settings settings) {
        super(settings.maxCount(1).maxDamage(DAMAGE_CAP));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            reload(world, user, user.getStackInHand(hand));
        }
        return ActionResult.CONSUME;
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

    public static boolean fireIfPossible(ServerWorld world, PlayerEntity player, Hand hand, boolean scoped) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(ModItems.AK47)) {
            return false;
        }

        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return false;
        }

        if (getAmmo(stack) <= 0) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.45F, 1.6F);
            return false;
        }

        AkBulletEntity bullet = new AkBulletEntity(ModEntities.AK47_BULLET, world, player);
        float divergence = scoped ? 0.12F : 1.35F;
        bullet.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 5.4F, divergence);
        world.spawnEntity(bullet);

        stack.setDamage(Math.min(MAGAZINE_SIZE, stack.getDamage() + 1));
        player.getItemCooldownManager().set(stack, FIRE_INTERVAL_TICKS);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 0.55F, scoped ? 0.75F : 0.72F);

        Vec3d muzzle = player.getEyePos().add(player.getRotationVec(1.0F).multiply(0.8D));
        world.spawnParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 1, 0.01D, 0.01D, 0.01D, 0.001D);
        world.spawnParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 1, 0.008D, 0.008D, 0.008D, 0.002D);
        return true;
    }

    public static boolean reloadIfPossible(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!stack.isOf(ModItems.AK47) || getAmmo(stack) >= MAGAZINE_SIZE) {
            return false;
        }

        reload(world, user, stack);
        return true;
    }

    public static int getAmmo(ItemStack stack) {
        return Math.max(0, MAGAZINE_SIZE - stack.getDamage());
    }

    private static void reload(World world, PlayerEntity user, ItemStack stack) {
        if (getAmmo(stack) >= MAGAZINE_SIZE) {
            return;
        }

        stack.setDamage(0);
        user.getItemCooldownManager().set(stack, RELOAD_TICKS);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 0.8F, 0.85F);
    }
}
