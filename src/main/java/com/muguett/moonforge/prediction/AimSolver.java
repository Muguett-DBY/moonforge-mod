package com.muguett.moonforge.prediction;

import com.muguett.moonforge.physics.ProjectilePhysicsProfile;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class AimSolver {
    private AimSolver() {
    }

    public static AimRecommendation solve(Vec3d shooterPos, Vec3d currentAimDirection, Vec3d targetCenter, Vec3d targetVelocity,
                                          ProjectilePhysicsProfile profile, boolean useMotionPrediction, double predictionMultiplier) {
        double flightTicks = Math.max(1.0D, shooterPos.distanceTo(targetCenter) / profile.launchSpeed());
        Vec3d predictedTarget = targetCenter;
        Vec3d solvedDirection = currentAimDirection;

        for (int iteration = 0; iteration < 4; iteration++) {
            predictedTarget = useMotionPrediction
                    ? MotionPredictor.predictPosition(targetCenter, targetVelocity, flightTicks, predictionMultiplier)
                    : targetCenter;

            Vec3d displacement = predictedTarget.subtract(shooterPos);
            Vec3d horizontal = new Vec3d(displacement.x, 0.0D, displacement.z);
            double horizontalDistance = Math.max(0.0001D, horizontal.length());
            double speed = profile.launchSpeed();
            double gravity = profile.gravityPerTick();
            double speedSquared = speed * speed;
            double rootTerm = (speedSquared * speedSquared) - gravity * (gravity * horizontalDistance * horizontalDistance + 2.0D * displacement.y * speedSquared);

            if (rootTerm < 0.0D) {
                solvedDirection = displacement.normalize();
                flightTicks = Math.max(1.0D, displacement.length() / speed);
                break;
            }

            double sqrt = Math.sqrt(rootTerm);
            double pitch = Math.atan((speedSquared - sqrt) / (gravity * horizontalDistance));
            Vec3d horizontalUnit = horizontal.normalize();
            solvedDirection = new Vec3d(
                    horizontalUnit.x * Math.cos(pitch),
                    Math.sin(pitch),
                    horizontalUnit.z * Math.cos(pitch)
            ).normalize();

            // Educational estimate: time-to-target ~= horizontal distance / horizontal speed.
            flightTicks = horizontalDistance / Math.max(0.0001D, speed * Math.cos(pitch));
        }

        double targetYaw = Math.toDegrees(Math.atan2(-solvedDirection.x, solvedDirection.z));
        double targetPitch = Math.toDegrees(-Math.asin(solvedDirection.y));
        double currentYaw = Math.toDegrees(Math.atan2(-currentAimDirection.x, currentAimDirection.z));
        double currentPitch = Math.toDegrees(-Math.asin(currentAimDirection.y));

        return new AimRecommendation(
                predictedTarget,
                solvedDirection,
                flightTicks,
                MathHelper.wrapDegrees((float) (targetYaw - currentYaw)),
                MathHelper.wrapDegrees((float) (targetPitch - currentPitch))
        );
    }
}
