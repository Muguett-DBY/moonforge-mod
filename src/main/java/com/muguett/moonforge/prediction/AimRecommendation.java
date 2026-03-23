package com.muguett.moonforge.prediction;

import net.minecraft.util.math.Vec3d;

public record AimRecommendation(
        Vec3d predictedTargetPosition,
        Vec3d recommendedAimDirection,
        double estimatedFlightTicks,
        double horizontalErrorDegrees,
        double verticalErrorDegrees
) {
}
