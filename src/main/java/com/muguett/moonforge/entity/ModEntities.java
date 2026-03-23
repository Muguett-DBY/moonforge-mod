package com.muguett.moonforge.entity;

import com.muguett.moonforge.MoonforgeMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static final EntityType<VoidburstBoltEntity> VOIDBURST_BOLT = register("voidburst_bolt",
            EntityType.Builder.<VoidburstBoltEntity>create(VoidburstBoltEntity::new, SpawnGroup.MISC)
                    .dimensions(0.5F, 0.5F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10));

    public static final EntityType<GuidedArrowEntity> GUIDED_ARROW = register("guided_arrow",
            EntityType.Builder.<GuidedArrowEntity>create(GuidedArrowEntity::new, SpawnGroup.MISC)
                    .dimensions(0.5F, 0.5F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10));

    public static final EntityType<ArsenalSpellEntity> ARSENAL_SPELL = register("arsenal_spell",
            EntityType.Builder.<ArsenalSpellEntity>create(ArsenalSpellEntity::new, SpawnGroup.MISC)
                    .dimensions(0.45F, 0.45F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10));

    private ModEntities() {
    }

    public static void initialize() {
    }

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String path, EntityType.Builder<T> builder) {
        Identifier id = Identifier.of(MoonforgeMod.MOD_ID, path);
        return Registry.register(Registries.ENTITY_TYPE, id, builder.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id)));
    }
}
