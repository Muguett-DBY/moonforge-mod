package com.muguett.moonforge.physics;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class TrajectorySimulator {
    private TrajectorySimulator() {
    }

    public static TrajectoryResult simulate(ClientWorld world, Entity shooter, Vec3d start, Vec3d initialVelocity,
                                            ProjectilePhysicsProfile profile) {
        List<TrajectoryPoint> points = new ArrayList<>();
        Vec3d position = start;
        Vec3d velocity = initialVelocity;

        points.add(new TrajectoryPoint(position, 0));

        for (int step = 1; step <= profile.maxSimulationSteps(); step++) {
            Vec3d nextPosition = position.add(velocity);

            BlockHitResult blockHit = world.raycast(new RaycastContext(
                    position,
                    nextPosition,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    shooter
            ));

            EntityHitResult entityHit = findEntityCollision(world, shooter, position, nextPosition, profile.collisionRadius());
            HitResult nearestHit = chooseNearestHit(position, blockHit, entityHit);

            if (nearestHit != null && nearestHit.getType() != HitResult.Type.MISS) {
                points.add(new TrajectoryPoint(nearestHit.getPos(), step));
                return new TrajectoryResult(profile.weaponType(), List.copyOf(points), nearestHit.getPos(), nearestHit.getType());
            }

            position = nextPosition;
            points.add(new TrajectoryPoint(position, step));

            // Minecraft projectile stepping stays visible here for learning:
            // p_next = p + v
            // v_dragged = v * drag
            // v_next = v_dragged + (0, -gravity, 0)
            velocity = velocity.multiply(profile.drag()).add(0.0D, -profile.gravityPerTick(), 0.0D);
        }

        return new TrajectoryResult(profile.weaponType(), List.copyOf(points), position, HitResult.Type.MISS);
    }

    private static EntityHitResult findEntityCollision(ClientWorld world, Entity shooter, Vec3d start, Vec3d end,
                                                       double collisionRadius) {
        Box searchBox = new Box(start, end).expand(collisionRadius);

        return world.getOtherEntities(shooter, searchBox, entity -> entity.isAlive() && !entity.isSpectator()).stream()
                .map(entity -> raycastEntity(entity, start, end, collisionRadius))
                .flatMap(Optional::stream)
                .min(Comparator.comparingDouble(hit -> hit.getPos().squaredDistanceTo(start)))
                .orElse(null);
    }

    private static Optional<EntityHitResult> raycastEntity(Entity entity, Vec3d start, Vec3d end, double collisionRadius) {
        return entity.getBoundingBox()
                .expand(collisionRadius)
                .raycast(start, end)
                .map(hitPos -> new EntityHitResult(entity, hitPos));
    }

    private static HitResult chooseNearestHit(Vec3d origin, BlockHitResult blockHit, EntityHitResult entityHit) {
        HitResult nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        if (blockHit != null && blockHit.getType() != HitResult.Type.MISS) {
            nearest = blockHit;
            nearestDistance = blockHit.getPos().squaredDistanceTo(origin);
        }

        if (entityHit != null) {
            double entityDistance = entityHit.getPos().squaredDistanceTo(origin);
            if (entityDistance < nearestDistance) {
                nearest = entityHit;
            }
        }

        return nearest;
    }
}