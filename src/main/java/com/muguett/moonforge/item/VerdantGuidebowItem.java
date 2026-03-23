package com.muguett.moonforge.item;

import com.muguett.moonforge.config.MoonforgeConfig;
import com.muguett.moonforge.config.MoonforgeConfigManager;
import com.muguett.moonforge.entity.GuidedArrowEntity;
import com.muguett.moonforge.entity.ModEntities;
import com.muguett.moonforge.render.GuidedCameraController;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class VerdantGuidebowItem extends BowItem {
    public VerdantGuidebowItem(Settings settings) {
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
        float pullProgress = BowItem.getPullProgress(useTicks);
        if (pullProgress < 0.15F) {
            return false;
        }

        MoonforgeConfig config = MoonforgeConfigManager.getConfig();
        if (!world.isClient()) {
            GuidedArrowEntity arrow = new GuidedArrowEntity(ModEntities.GUIDED_ARROW, world, player);
            arrow.setCustomGravity(config.guidebowGravity);
            arrow.setSteerStrength(config.guidebowSteerStrength);
            arrow.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, (float) (config.guidebowSpeed + pullProgress * 0.45F), 0.0F);
            world.spawnEntity(arrow);
        } else {
            GuidedCameraController.beginTrackingWindow();
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 0.95F);
        player.getItemCooldownManager().set(stack, 12);
        return true;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}
