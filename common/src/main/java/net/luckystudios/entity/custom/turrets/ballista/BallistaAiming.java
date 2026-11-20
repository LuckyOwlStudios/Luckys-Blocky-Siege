package net.luckystudios.entity.custom.turrets.ballista;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Works out where the ballista has to point so its shot lands on a (possibly moving) target.
 * Projectile flight is simulated tick by tick with the same physics vanilla arrows use, so
 * long shots get a proper arc, and the target's current velocity is used to lead it.
 */
public final class BallistaAiming {
    /** Arrows: per-tick drag and gravity from AbstractArrow. */
    public static final double ARROW_GRAVITY = 0.05;
    public static final double ARROW_DRAG = 0.99;
    /** Crossbow-fired fireworks fly in a straight line. */
    public static final double ROCKET_GRAVITY = 0.0;
    public static final double ROCKET_DRAG = 1.0;

    private static final double MIN_ELEVATION = Math.toRadians(-60.0);
    private static final double MAX_ELEVATION = Math.toRadians(45.0);
    private static final int MAX_FLIGHT_TICKS = 200;
    private static final int LEAD_ITERATIONS = 4;
    private static final int BISECTION_STEPS = 24;

    private BallistaAiming() {
    }

    /**
     * @param yRot      yaw in Minecraft degrees
     * @param xRot      pitch in Minecraft degrees (negative is up)
     * @param direction unit vector the projectile should leave along
     */
    public record Solution(float yRot, float xRot, Vec3 direction, double flightTicks) {
    }

    /** Where crossbow projectiles spawn for this shooter (matches ArrowItem/CrossbowItem). */
    public static Vec3 projectileOrigin(LivingEntity shooter) {
        return new Vec3(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }

    public static Solution solve(LivingEntity shooter, LivingEntity target, double speed, double gravity, double drag) {
        Vec3 origin = projectileOrigin(shooter);
        Vec3 aimPoint = new Vec3(target.getX(), target.getY(0.5), target.getZ());
        // Measured from the last tick's movement, which also works for players (their server-side
        // delta movement is not reliable). Vertical motion is ignored so jumping doesn't throw the aim off.
        Vec3 targetVelocity = new Vec3(target.getX() - target.xo, 0.0, target.getZ() - target.zo);

        double flightTicks = 0.0;
        double elevation = 0.0;
        double dx = 0.0;
        double dz = 0.0;
        for (int i = 0; i < LEAD_ITERATIONS; i++) {
            Vec3 predicted = aimPoint.add(targetVelocity.scale(flightTicks));
            dx = predicted.x - origin.x;
            dz = predicted.z - origin.z;
            double dy = predicted.y - origin.y;
            double distance = Math.sqrt(dx * dx + dz * dz);
            double[] result = solveElevation(distance, dy, speed, gravity, drag);
            elevation = result[0];
            flightTicks = result[1];
        }

        float yRot = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float xRot = (float) (-elevation * Mth.RAD_TO_DEG);
        double horizontal = Math.cos(elevation);
        double yawRad = yRot * Mth.DEG_TO_RAD;
        Vec3 direction = new Vec3(-Math.sin(yawRad) * horizontal, Math.sin(elevation), Math.cos(yawRad) * horizontal);
        return new Solution(Mth.wrapDegrees(yRot), xRot, direction, flightTicks);
    }

    /** Returns {elevation in radians, flight time in ticks} for the low arc that hits (distance, dy). */
    private static double[] solveElevation(double distance, double dy, double speed, double gravity, double drag) {
        if (distance < 1.0E-3) {
            return new double[]{dy >= 0 ? MAX_ELEVATION : MIN_ELEVATION, 0.0};
        }
        double low = MIN_ELEVATION;
        double high = MAX_ELEVATION;
        for (int i = 0; i < BISECTION_STEPS; i++) {
            double mid = (low + high) * 0.5;
            double[] hit = heightAt(mid, distance, speed, gravity, drag);
            if (hit == null || hit[0] < dy) {
                low = mid;
            } else {
                high = mid;
            }
        }
        double elevation = (low + high) * 0.5;
        double[] hit = heightAt(elevation, distance, speed, gravity, drag);
        double ticks = hit != null ? hit[1] : distance / Math.max(speed, 1.0E-3);
        return new double[]{elevation, ticks};
    }

    /** Simulates a shot and returns {height, ticks} when it has travelled the horizontal distance, or null if it never does. */
    private static double[] heightAt(double elevation, double distance, double speed, double gravity, double drag) {
        double h = 0.0;
        double y = 0.0;
        double vh = speed * Math.cos(elevation);
        double vy = speed * Math.sin(elevation);
        for (int tick = 0; tick < MAX_FLIGHT_TICKS; tick++) {
            if (h + vh >= distance) {
                double fraction = vh > 1.0E-6 ? (distance - h) / vh : 0.0;
                return new double[]{y + vy * fraction, tick + fraction};
            }
            // Same order as AbstractArrow#tick: move, then drag, then gravity.
            h += vh;
            y += vy;
            vh *= drag;
            vy = vy * drag - gravity;
            if (vh < 1.0E-4) {
                break;
            }
        }
        return null;
    }
}
