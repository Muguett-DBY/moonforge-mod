package com.muguett.moonforge.item;

import com.muguett.moonforge.MoonforgeMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
    public static final ItemGroup MOONFORGE_ARSENAL = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(MoonforgeMod.MOD_ID, "arsenal"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.VOIDBURST_ARBALEST))
                    .displayName(Text.translatable("itemGroup.moonforge.arsenal"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.VOIDBURST_ARBALEST);
                        entries.add(ModItems.VERDANT_GUIDEBOW);
                        entries.add(ModItems.SOLARIS_STAFF);
                        entries.add(ModItems.FROSTBITE_SCEPTER);
                        entries.add(ModItems.THUNDERCHAIN_BATON);
                        entries.add(ModItems.GRAVITON_MAUL);
                        entries.add(ModItems.BLOOMLASH_WHIP);
                        entries.add(ModItems.RIFTSTEP_DAGGER);
                        entries.add(ModItems.ECHO_DISC_LAUNCHER);
                        entries.add(ModItems.STARFALL_TOME);
                    })
                    .build()
    );

    private ModItemGroups() {
    }

    public static void initialize() {
    }
}
