package com.muguett.moonforge.item;

import com.muguett.moonforge.config.MoonforgeConfig;
import com.muguett.moonforge.config.MoonforgeConfigManager;
import com.muguett.moonforge.entity.ModEntities;
import com.muguett.moonforge.entity.VoidburstBoltEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class VoidburstArbalestItem extends Item {
    private static final int CHARGE_TICKS = 20;

    public VoidburstArbalestItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            return false;
        }

        int useTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;
        if (useTicks < CHARGE_TICKS) {
            return false;
        }

        MoonforgeConfig config = MoonforgeConfigManager.getConfig();
        if (!world.isClient()) {
            VoidburstBoltEntity bolt = new VoidburstBoltEntity(ModEntities.VOIDBURST_BOLT, world, player);
            bolt.setCustomGravity(config.voidburstGravity);
            bolt.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, (float) config.voidburstSpeed, 0.05F);
            world.spawnEntity(bolt);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, 0.65F);
        player.getItemCooldownManager().set(stack, 24);
        return true;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.CROSSBOW;
    }
}
