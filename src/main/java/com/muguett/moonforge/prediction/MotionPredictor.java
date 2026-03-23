package com.muguett.moonforge.prediction;

import net.minecraft.util.math.Vec3d;

public final class MotionPredictor {
    private MotionPredictor() {
    }

    public static Vec3d predictPosition(Vec3d currentPosition, Vec3d velocity, double flightTicks, double multiplier) {
        return currentPosition.add(velocity.multiply(flightTicks * multiplier));
    }
}
