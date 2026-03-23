package com.muguett.moonforge.item;

import net.minecraft.sound.SoundEvent;

public record FirearmStats(
        String scopeLabel,
        int scopeDivisor,
        FireMode fireMode,
        int magazineSize,
        int fireIntervalTicks,
        int reloadTicks,
        int pellets,
        float muzzleVelocity,
        float scopedDivergence,
        float hipDivergence,
        float damage,
        double gravity,
        float scopedPitchKick,
        float hipPitchKick,
        float scopedYawKick,
        float hipYawKick,
        SoundEvent fireSound,
        float fireVolume,
        float firePitch,
        SoundEvent reloadSound,
        float reloadVolume,
        float reloadPitch,
        int flameParticles,
        int smokeParticles,
        int trailStyle
) {
}
