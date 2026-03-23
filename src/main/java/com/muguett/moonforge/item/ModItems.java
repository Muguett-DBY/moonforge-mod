package com.muguett.moonforge.item;

import com.muguett.moonforge.MoonforgeMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModItems {
    public static final Item VOIDBURST_ARBALEST = register("voidburst_arbalest", settings -> new VoidburstArbalestItem(settings.maxCount(1)));
    public static final Item VERDANT_GUIDEBOW = register("verdant_guidebow", settings -> new VerdantGuidebowItem(settings.maxCount(1)));
    public static final Item AK47 = register("ak47", Ak47Item::new);
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
