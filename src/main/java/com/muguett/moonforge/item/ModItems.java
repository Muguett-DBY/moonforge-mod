package com.muguett.moonforge.item;

import com.muguett.moonforge.MoonforgeMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModItems {
    public static final Item VOIDBURST_ARBALEST = register("voidburst_arbalest", settings -> new VoidburstArbalestItem(settings.maxCount(1)));
    public static final Item VERDANT_GUIDEBOW = register("verdant_guidebow", settings -> new VerdantGuidebowItem(settings.maxCount(1)));
    public static final Item AK47 = register("ak47", settings -> new FirearmItem(settings, new FirearmStats("6.0x", 6, FireMode.AUTO, 30, 2, 24, 1, 5.4F, 0.12F, 1.35F, 8.0F, 0.0023D, 0.18F, 0.6F, 0.03F, 0.12F, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.55F, 0.75F, SoundEvents.ITEM_CROSSBOW_LOADING_END.value(), 0.8F, 0.85F, 1, 1, 0)));
    public static final Item M4_CARBINE = register("m4_carbine", settings -> new FirearmItem(settings, new FirearmStats("4.0x", 4, FireMode.AUTO, 32, 2, 20, 1, 5.8F, 0.09F, 1.05F, 7.0F, 0.0020D, 0.16F, 0.5F, 0.025F, 0.1F, SoundEvents.ENTITY_ARROW_SHOOT, 0.9F, 0.55F, SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE.value(), 0.75F, 1.05F, 1, 1, 1)));
    public static final Item VIPER_SMG = register("viper_smg", settings -> new FirearmItem(settings, new FirearmStats("1.5x", 2, FireMode.AUTO, 40, 1, 18, 1, 4.9F, 0.2F, 1.9F, 5.5F, 0.0018D, 0.12F, 0.42F, 0.02F, 0.1F, SoundEvents.ENTITY_BLAZE_SHOOT, 0.45F, 1.65F, SoundEvents.ITEM_BUNDLE_INSERT, 0.7F, 1.25F, 1, 0, 2)));
    public static final Item THUNDERCLAP_SHOTGUN = register("thunderclap_shotgun", settings -> new FirearmItem(settings, new FirearmStats("2.0x", 2, FireMode.SEMI, 8, 10, 30, 7, 4.2F, 0.75F, 2.25F, 3.2F, 0.0045D, 0.28F, 1.0F, 0.04F, 0.16F, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.52F, 1.35F, SoundEvents.ITEM_ARMOR_EQUIP_IRON.value(), 0.85F, 0.7F, 2, 1, 3)));
    public static final Item PEREGRINE_DMR = register("peregrine_dmr", settings -> new FirearmItem(settings, new FirearmStats("8.0x", 8, FireMode.SEMI, 12, 6, 26, 1, 6.6F, 0.03F, 0.8F, 14.0F, 0.0012D, 0.22F, 0.72F, 0.02F, 0.09F, SoundEvents.ITEM_TRIDENT_THROW.value(), 0.7F, 1.45F, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, 0.85F, 1.2F, 2, 0, 4)));
    public static final Item AK47_ROUND = register("ak47_round", Item::new);
    public static final Item SOLARIS_STAFF = register("solaris_staff", settings -> new AbilityWeaponItem(settings, ArsenalWeaponType.SOLARIS_STAFF, 18));
    public static final Item FROSTBITE_SCEPTER = register("frostbite_scepter", settings -> new AbilityWeaponItem(settings, ArsenalWeaponType.FROSTBITE_SCEPTER, 14));
    public static final Item THUNDERCHAIN_BATON = register("thunderchain_baton", settings -> new AbilityWeaponItem(settings, ArsenalWeaponType.THUNDERCHAIN_BATON, 22));
    public static final Item GRAVITON_MAUL = register("graviton_maul", settings -> new AbilityWeaponItem(settings, ArsenalWeaponType.GRAVITON_MAUL, 24));
    public static final Item BLOOMLASH_WHIP = register("bloomlash_whip", settings -> new AbilityWeaponItem(settings, ArsenalWeaponType.BLOOMLASH_WHIP, 10));
    public static final Item RIFTSTEP_DAGGER = register("riftstep_dagger", settings -> new AbilityWeaponItem(settings, ArsenalWeaponType.RIFTSTEP_DAGGER, 26));
    public static final Item ECHO_DISC_LAUNCHER = register("echo_disc_launcher", settings -> new AbilityWeaponItem(settings, ArsenalWeaponType.ECHO_DISC_LAUNCHER, 16));
    public static final Item STARFALL_TOME = register("starfall_tome", settings -> new AbilityWeaponItem(settings, ArsenalWeaponType.STARFALL_TOME, 30));

    private ModItems() {
    }

    public static void initialize() {
    }

    private static Item register(String path, Function<Item.Settings, Item> factory) {
        Identifier id = Identifier.of(MoonforgeMod.MOD_ID, path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item item = factory.apply(new Item.Settings().registryKey(key));
        return Registry.register(Registries.ITEM, id, item);
    }
}
