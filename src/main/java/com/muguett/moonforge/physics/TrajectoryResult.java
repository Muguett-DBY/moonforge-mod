package com.muguett.moonforge.physics;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public record TrajectoryResult(
        ProjectileWeaponType weaponType,
        List<TrajectoryPoint> points,
        Vec3d impactPosition,
        HitResult.Type impactType
) {
}