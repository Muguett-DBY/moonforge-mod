package com.muguett.moonforge.item;

import com.muguett.moonforge.entity.ArsenalSpellEntity;
import com.muguett.moonforge.entity.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class AbilityWeaponItem extends Item {
    private final ArsenalWeaponType type;
    private final int cooldownTicks;
    private final float launchSpeed;
    private final float launchDivergence;

    public AbilityWeaponItem(Settings settings, ArsenalWeaponType type, int cooldownTicks) {
        this(settings, type, cooldownTicks, 1.8F, 0.05F);
    }

    public AbilityWeaponItem(Settings settings, ArsenalWeaponType type, int cooldownTicks, float launchSpeed, float launchDivergence) {
        super(settings.maxCount(1));
        this.type = type;
        this.cooldownTicks = cooldownTicks;
        this.launchSpeed = launchSpeed;
        this.launchDivergence = launchDivergence;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world instanceof ServerWorld serverWorld) {
            ArsenalSpellEntity projectile = new ArsenalSpellEntity(ModEntities.ARSENAL_SPELL, serverWorld, user, stack.copy());
            projectile.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, launchSpeed, launchDivergence);
            serverWorld.spawnEntity(projectile);
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(), castSound(), SoundCategory.PLAYERS, 1.0F, castPitch());
        }
        user.getItemCooldownManager().set(stack, cooldownTicks);
        return ActionResult.CONSUME;
    }

    private net.minecraft.sound.SoundEvent castSound() {
        return switch (type) {
            case SOLARIS_STAFF -> SoundEvents.ITEM_FIRECHARGE_USE;
            case FROSTBITE_SCEPTER -> SoundEvents.BLOCK_POWDER_SNOW_BREAK;
            case THUNDERCHAIN_BATON -> SoundEvents.BLOCK_BEACON_POWER_SELECT;
            case GRAVITON_MAUL -> SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE;
            case BLOOMLASH_WHIP -> SoundEvents.ITEM_BONE_MEAL_USE;
            case RIFTSTEP_DAGGER -> SoundEvents.ENTITY_ENDERMAN_AMBIENT;
            case ECHO_DISC_LAUNCHER -> SoundEvents.BLOCK_SCULK_CATALYST_BLOOM;
            case STARFALL_TOME -> SoundEvents.ENTITY_BLAZE_AMBIENT;
        };
    }

    private float castPitch() {
        return switch (type) {
            case SOLARIS_STAFF -> 1.1F;
            case FROSTBITE_SCEPTER -> 0.75F;
            case THUNDERCHAIN_BATON -> 0.9F;
            case GRAVITON_MAUL -> 0.6F;
            case BLOOMLASH_WHIP -> 1.2F;
            case RIFTSTEP_DAGGER -> 1.4F;
            case ECHO_DISC_LAUNCHER -> 1.35F;
            case STARFALL_TOME -> 0.7F;
        };
    }
}
