package net.luckystudios.entity.ai.crew;

/**
 * Works out the barrel pitch that lands a projectile on a target, by stepping the same simple physics that the
 * mod's projectiles use each tick: move by velocity, then scale velocity by drag and subtract gravity.
 */
public final class WeaponBallistics {

    private static final int MAX_FLIGHT_TICKS = 160;
    private static final float SCAN_STEP = 1.5F;
    private static final int REFINE_STEPS = 10;

    private WeaponBallistics() {
    }

    /** How a weapon throws its current ammo. */
    public record Profile(float speed, double gravity, double drag, float muzzleOffset) {
    }

    /**
     * The result of a firing solution.
     *
     * @param pitch       barrel pitch in degrees, positive is up
     * @param flightTicks how long the shot takes to reach the target
     * @param reachable   whether the pitch actually puts the shot on the target
     */
    public record Solution(float pitch, int flightTicks, boolean reachable) {
    }

    /**
     * Finds the lowest (most direct) pitch inside {@code [minPitch, maxPitch]} whose trajectory passes the target.
     *
     * @param horizontal horizontal distance from the weapon's pivot to the target
     * @param vertical   height of the target above the weapon's pivot
     * @param tolerance  how far above the target a shot may still pass when the barrel can't depress any further
     */
    public static Solution solve(Profile profile, double horizontal, double vertical, float minPitch, float maxPitch, double tolerance) {
        // A shot from the lowest pitch already passing above the target means the target is too low to reach
        Flight lowest = fly(profile, minPitch, horizontal);
        if (lowest.reached && lowest.height >= vertical) {
            boolean closeEnough = lowest.height - vertical <= tolerance;
            return new Solution(minPitch, lowest.ticks, closeEnough);
        }

        float previous = minPitch;
        Flight best = lowest;
        float bestPitch = minPitch;
        for (float pitch = minPitch + SCAN_STEP; pitch <= maxPitch + 0.001F; pitch += SCAN_STEP) {
            float clamped = Math.min(pitch, maxPitch);
            Flight flight = fly(profile, clamped, horizontal);
            if (flight.reached && flight.height >= vertical) {
                return refine(profile, previous, clamped, horizontal, vertical);
            }
            if (flight.reached && (!best.reached || flight.height > best.height)) {
                best = flight;
                bestPitch = clamped;
            }
            previous = clamped;
        }

        // Out of range: point the barrel at the angle that gets closest, but report that it can't land
        return new Solution(bestPitch, best.ticks, false);
    }

    /** How many ticks a shot at this pitch takes to cover the horizontal distance. */
    public static int flightTime(Profile profile, float pitch, double horizontal) {
        return fly(profile, pitch, horizontal).ticks;
    }

    private static Solution refine(Profile profile, float low, float high, double horizontal, double vertical) {
        Flight hit = fly(profile, high, horizontal);
        for (int i = 0; i < REFINE_STEPS; i++) {
            float mid = (low + high) * 0.5F;
            Flight flight = fly(profile, mid, horizontal);
            if (flight.reached && flight.height >= vertical) {
                high = mid;
                hit = flight;
            } else {
                low = mid;
            }
        }
        return new Solution(high, hit.ticks, true);
    }

    private record Flight(boolean reached, double height, int ticks) {
    }

    private static Flight fly(Profile profile, float pitchDegrees, double horizontal) {
        double radians = Math.toRadians(pitchDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double x = cos * profile.muzzleOffset();
        double y = sin * profile.muzzleOffset();
        if (x >= horizontal) {
            return new Flight(true, y, 0);
        }
        double vx = cos * profile.speed();
        double vy = sin * profile.speed();

        for (int tick = 1; tick <= MAX_FLIGHT_TICKS; tick++) {
            double nextX = x + vx;
            double nextY = y + vy;
            if (nextX >= horizontal) {
                double fraction = (horizontal - x) / (nextX - x);
                return new Flight(true, y + (nextY - y) * fraction, tick);
            }
            x = nextX;
            y = nextY;
            vx *= profile.drag();
            vy = vy * profile.drag() - profile.gravity();
            if (vx < 1.0E-3 || y < -128.0) {
                break;
            }
        }
        return new Flight(false, Double.NEGATIVE_INFINITY, MAX_FLIGHT_TICKS);
    }
}
