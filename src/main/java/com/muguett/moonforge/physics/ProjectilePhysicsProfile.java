package com.muguett.moonforge.physics;

public record ProjectilePhysicsProfile(
        ProjectileWeaponType weaponType,
        double launchSpeed,
        double gravityPerTick,
        double drag,
        double collisionRadius,
        int maxSimulationSteps
) {
    public static ProjectilePhysicsProfile bow(double launchSpeed) {
        return new ProjectilePhysicsProfile(ProjectileWeaponType.BOW, launchSpeed, 0.05D, 0.99D, 0.25D, 120);
    }

    public static ProjectilePhysicsProfile crossbow() {
        return new ProjectilePhysicsProfile(ProjectileWeaponType.CROSSBOW, 3.15D, 0.05D, 0.99D, 0.25D, 140);
    }

    public static ProjectilePhysicsProfile trident() {
        return new ProjectilePhysicsProfile(ProjectileWeaponType.TRIDENT, 2.5D, 0.05D, 0.99D, 0.25D, 120);
    }
}