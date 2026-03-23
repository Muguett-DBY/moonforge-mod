package com.muguett.moonforge.item;

import com.muguett.moonforge.entity.AkBulletEntity;
import com.muguett.moonforge.entity.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FirearmItem extends Item {
    private final FirearmStats stats;

    public FirearmItem(Settings settings, FirearmStats stats) {
        super(settings.maxCount(1).maxDamage(stats.magazineSize() + 1));
        this.stats = stats;
    }

    public FirearmStats stats() {
        return stats;
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
        FirearmStats firearmStats = getStats(stack);
        return firearmStats == null ? 0 : Math.round(13.0F * getAmmo(stack) / firearmStats.magazineSize());
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
        FirearmStats stats = getStats(stack);
        if (stats == null) {
            return false;
        }

        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return false;
        }

        if (getAmmo(stack) <= 0) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.45F, 1.6F);
            return false;
        }

        for (int pellet = 0; pellet < stats.pellets(); pellet++) {
            AkBulletEntity bullet = new AkBulletEntity(ModEntities.AK47_BULLET, world, player);
            bullet.setBaseDamage(stats.damage());
            bullet.setGravityScale(stats.gravity());
            bullet.setTrailStyle(stats.trailStyle());
            float divergence = scoped ? stats.scopedDivergence() : stats.hipDivergence();
            bullet.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, stats.muzzleVelocity(), divergence);
            world.spawnEntity(bullet);
        }

        stack.setDamage(Math.min(stats.magazineSize(), stack.getDamage() + 1));
        player.getItemCooldownManager().set(stack, stats.fireIntervalTicks());
        world.playSound(null, player.getX(), player.getY(), player.getZ(), stats.fireSound(), SoundCategory.PLAYERS, stats.fireVolume(), stats.firePitch());

        Vec3d muzzle = player.getEyePos().add(player.getRotationVec(1.0F).multiply(0.8D));
        world.spawnParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, stats.flameParticles(), 0.01D, 0.01D, 0.01D, 0.001D);
        world.spawnParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, stats.smokeParticles(), 0.008D, 0.008D, 0.008D, 0.002D);
        return true;
    }

    public static boolean reloadIfPossible(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        FirearmStats stats = getStats(stack);
        if (stats == null || getAmmo(stack) >= stats.magazineSize()) {
            return false;
        }

        reload(world, user, stack);
        return true;
    }

    public static int getAmmo(ItemStack stack) {
        FirearmStats stats = getStats(stack);
        return stats == null ? 0 : Math.max(0, stats.magazineSize() - stack.getDamage());
    }

    public static FirearmStats getStats(ItemStack stack) {
        return stack.getItem() instanceof FirearmItem firearm ? firearm.stats() : null;
    }

    private static void reload(World world, PlayerEntity user, ItemStack stack) {
        FirearmStats stats = getStats(stack);
        if (stats == null || getAmmo(stack) >= stats.magazineSize()) {
            return;
        }

        stack.setDamage(0);
        user.getItemCooldownManager().set(stack, stats.reloadTicks());
        world.playSound(null, user.getX(), user.getY(), user.getZ(), stats.reloadSound(), SoundCategory.PLAYERS, stats.reloadVolume(), stats.reloadPitch());
    }
}
