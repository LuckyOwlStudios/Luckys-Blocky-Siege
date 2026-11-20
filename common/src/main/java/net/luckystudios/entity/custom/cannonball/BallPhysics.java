package net.luckystudios.entity.custom.cannonball;

import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The maths behind rolling cannonballs: how a heavy iron ball skims, bounces, spins up and slows down
 * on different kinds of ground. Everything is in blocks and ticks, with the same gravity (0.05) and air
 * drag (0.99) as the ball's flight, so free flight is unchanged and aiming still works.
 *
 * <p>The model, in short:
 * <ul>
 *     <li><b>Skim (ricochet)</b>: a fast ball that meets a surface at a shallow angle skips off it,
 *     keeping most of its speed and leaving at a flatter angle than it came in. How shallow depends on the
 *     surface: stone and ice are easy to skip off, sand and snow swallow the ball.</li>
 *     <li><b>Bounce</b>: otherwise the ball rebounds with the surface's restitution, and loses less of it on
 *     gentle hits than on hard ones. Friction during the hit spins the ball up, which costs it at most
 *     2/7 of its sliding speed, the point where a solid ball rolls without slipping.</li>
 *     <li><b>Settle</b>: once a bounce off the ground is too small to see, the ball starts rolling.</li>
 *     <li><b>Roll</b>: rolling resistance slows it, a lot on sand, very little on ice, until it rests.</li>
 * </ul>
 */
public final class BallPhysics {

    private BallPhysics() {}

    /** Below this closing speed a bounce off the ground turns into rolling (the hop would be under 0.05 blocks). */
    public static final double SETTLE_SPEED = 0.07;
    /** A rolling ball slower than this comes to rest. */
    public static final double REST_SPEED = 0.012;
    /** Balls slower than this can't skim. */
    public static final double SKIM_MIN_SPEED = 0.6;
    /** How much more of the closing speed hard hits soak up than gentle ones. */
    private static final double HARD_HIT_SOFTENING = 0.25;
    /** Blocks softer than this (glass, leaves, snow) are smashed rather than skimmed off by a live ball. */
    public static final float FRAGILE_HARDNESS = 0.4F;
    /** A solid ball can lose at most this share of its sliding speed to friction before it rolls without slipping. */
    private static final double ROLLING_LIMIT = 2.0 / 7.0;

    /** Water: a fast ball coming in flatter than this skips across the surface like a stone. */
    public static final double WATER_SKIP_ANGLE = 14.0;
    public static final double WATER_SKIP_MIN_SPEED = 0.9;
    private static final double WATER_SKIP_RESTITUTION = 0.55;
    private static final double WATER_SKIP_KEEP = 0.8;

    /** Under water everything is damped. */
    private static final double UNDERWATER_RESTITUTION = 0.4;

    /** Below this speed a ball no longer hurts what it runs into. */
    public static final double MIN_DAMAGE_SPEED = 0.25;
    /** Share of its speed a ball keeps after smashing through something it killed. */
    public static final double PLOW_THROUGH_KEEP = 0.8;
    /** Share of its speed a live ball keeps after it detonates, as it carries on into the crater. */
    public static final double DETONATION_CARRY = 0.25;

    /** How a kind of ground treats a ball. */
    public enum Surface {
        //     restitution, friction, roll drag, roll decel, skim angle
        HARD  (0.50, 0.20, 0.982, 0.0018, 28.0),
        WOOD  (0.40, 0.25, 0.978, 0.0022, 24.0),
        EARTH (0.32, 0.35, 0.960, 0.0035, 20.0),
        LOOSE (0.14, 0.55, 0.900, 0.0060, 12.0),
        SOFT  (0.05, 0.80, 0.800, 0.0100, 0.0),
        GLASS (0.40, 0.15, 0.985, 0.0015, 0.0),
        ICE   (0.30, 0.03, 0.995, 0.0004, 32.0),
        SLIME (0.80, 0.30, 0.940, 0.0030, 40.0),
        HONEY (0.00, 1.00, 0.500, 0.0200, 0.0);

        /** Share of the closing speed given back by a gentle bounce. */
        public final double restitution;
        /** Grip during a hit, which turns sliding into spin. */
        public final double friction;
        /** Share of its speed a rolling ball keeps each tick... */
        public final double rollDrag;
        /** ...after first losing this much (blocks per tick). */
        public final double rollDecel;
        /** Steepest angle (degrees, measured from the surface) a ball can still skim off. */
        public final double skimAngle;

        Surface(double restitution, double friction, double rollDrag, double rollDecel, double skimAngle) {
            this.restitution = restitution;
            this.friction = friction;
            this.rollDrag = rollDrag;
            this.rollDecel = rollDecel;
            this.skimAngle = skimAngle;
        }

        /** Whether the ball should clang like iron on iron when it hits this. */
        public boolean ringsOnImpact() {
            return this == HARD || this == GLASS || this == ICE;
        }

        public static Surface of(BlockState state) {
            if (state.is(Blocks.SLIME_BLOCK)) return SLIME;
            if (state.is(Blocks.HONEY_BLOCK)) return HONEY;
            if (state.is(BlockTags.ICE)) return ICE;
            if (state.is(BlockTags.WOOL) || state.is(BlockTags.WOOL_CARPETS) || state.is(BlockTags.LEAVES)
                    || state.is(BlockTags.BEDS) || state.is(Blocks.HAY_BLOCK) || state.is(Blocks.SPONGE) || state.is(Blocks.WET_SPONGE)
                    || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MOSS_CARPET)) return SOFT;
            if (state.is(BlockTags.SAND) || state.is(BlockTags.SNOW) || state.is(Blocks.GRAVEL) || state.is(Blocks.SUSPICIOUS_GRAVEL)
                    || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.MUD) || state.is(Blocks.CLAY)) return LOOSE;
            SoundType sound = state.getSoundType();
            if (sound == SoundType.GLASS) return GLASS;
            if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return HARD;
            if (state.is(BlockTags.MINEABLE_WITH_AXE)) return WOOD;
            if (state.is(BlockTags.DIRT) || state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(BlockTags.MINEABLE_WITH_HOE)) return EARTH;
            // Anything else (mostly modded blocks without tool tags): judge it by how it sounds
            if (sound == SoundType.METAL || sound == SoundType.STONE || sound == SoundType.DEEPSLATE || sound == SoundType.NETHERITE_BLOCK
                    || sound == SoundType.ANVIL || sound == SoundType.COPPER || sound == SoundType.CHAIN) return HARD;
            if (sound == SoundType.WOOD || sound == SoundType.BAMBOO_WOOD || sound == SoundType.CHERRY_WOOD || sound == SoundType.NETHER_WOOD) return WOOD;
            if (sound == SoundType.SAND || sound == SoundType.GRAVEL || sound == SoundType.SNOW || sound == SoundType.SOUL_SAND
                    || sound == SoundType.SOUL_SOIL || sound == SoundType.MUD) return LOOSE;
            if (sound == SoundType.WOOL || sound == SoundType.GRASS || sound == SoundType.AZALEA_LEAVES || sound == SoundType.CHERRY_LEAVES) return SOFT;
            return EARTH;
        }
    }

    /**
     * What a hit did to the ball.
     *
     * @param velocity     the ball's velocity after the hit
     * @param closingSpeed how fast it was moving into the surface (drives sounds and dust)
     * @param skimmed      it skipped off at a shallow angle
     * @param settled      it was a landing too small to bounce, so the ball now rolls
     */
    public record Impact(Vec3 velocity, double closingSpeed, boolean skimmed, boolean settled) {}

    /** Unit vector pointing out of the given face. */
    public static Vec3 normalOf(Direction face) {
        return new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
    }

    /** Whether a ball moving like this skims off the surface instead of hitting it properly. */
    public static boolean skims(Vec3 velocity, Direction face, Surface surface) {
        return collide(velocity, face, surface, false).skimmed();
    }

    /**
     * Works out how a ball moving at {@code velocity} comes off the face {@code face} of a block made of
     * {@code surface}.
     */
    public static Impact collide(Vec3 velocity, Direction face, Surface surface, boolean underwater) {
        Vec3 normal = normalOf(face);
        double closing = -velocity.dot(normal);
        if (closing <= 1.0E-6) {
            // Already moving away (a glancing touch): nothing to do
            return new Impact(velocity, 0.0, false, false);
        }
        Vec3 sliding = velocity.add(normal.scale(closing));
        double slide = sliding.length();
        double speed = velocity.length();
        double angle = Math.toDegrees(Math.atan2(closing, slide));

        // Skim: fast and flat. Faster balls skip at a slightly steeper angle.
        if (!underwater && speed > SKIM_MIN_SPEED && surface.skimAngle > 0.0) {
            double critical = surface.skimAngle * Mth.clamp(0.5 + speed / 4.0, 0.6, 1.2);
            if (angle < critical) {
                double rebound = closing * (0.3 + 0.4 * surface.restitution) / (1.0 + 0.35 * closing);
                // Too small a hop to leave the ground isn't a skim, it's a landing
                if (rebound >= SETTLE_SPEED || face != Direction.UP) {
                    double keep = 1.0 - (0.1 + 0.3 * surface.friction);
                    return new Impact(sliding.scale(keep).add(normal.scale(rebound)), closing, true, false);
                }
            }
        }

        // Bounce: harder hits give back a smaller share
        double restitution = surface.restitution / (1.0 + HARD_HIT_SOFTENING * closing);
        if (underwater) restitution *= UNDERWATER_RESTITUTION;
        double rebound = closing * restitution;
        // Friction spins the ball up, but once it's rolling it stops scrubbing off speed
        double frictionLoss = slide > 1.0E-9 ? Math.min(surface.friction * (1.0 + restitution) * closing, slide * ROLLING_LIMIT) / slide : 0.0;
        Vec3 newSliding = sliding.scale(1.0 - frictionLoss);
        boolean settled = face == Direction.UP && rebound < SETTLE_SPEED;
        if (settled) {
            rebound = 0.0;
        }
        return new Impact(newSliding.add(normal.scale(rebound)), closing, false, settled);
    }

    /** Skipping off a water surface, or {@code null} if the ball goes in. */
    public static Vec3 skipOffWater(Vec3 velocity) {
        double down = -velocity.y;
        double across = velocity.horizontalDistance();
        if (down <= 0.0 || velocity.length() < WATER_SKIP_MIN_SPEED) return null;
        if (Math.toDegrees(Math.atan2(down, across)) >= WATER_SKIP_ANGLE) return null;
        return new Vec3(velocity.x * WATER_SKIP_KEEP, down * WATER_SKIP_RESTITUTION, velocity.z * WATER_SKIP_KEEP);
    }

    /** One tick of rolling resistance on a horizontal velocity. */
    public static Vec3 rollingResistance(Vec3 velocity, Surface surface) {
        double speed = velocity.horizontalDistance();
        if (speed < 1.0E-9) return new Vec3(0.0, velocity.y, 0.0);
        double slowed = Math.max(0.0, speed - surface.rollDecel) * surface.rollDrag;
        double scale = slowed / speed;
        return new Vec3(velocity.x * scale, velocity.y, velocity.z * scale);
    }

    /**
     * Glancing off a body: the ball bounces off the side of {@code target} and loses most of the push it
     * put into it. Bodies are upright, so the bounce is mostly sideways.
     */
    public static Vec3 glanceOff(Vec3 velocity, Vec3 ballCenter, AABB target) {
        Vec3 center = target.getCenter();
        Vec3 normal = new Vec3(ballCenter.x - center.x, (ballCenter.y - center.y) * 0.25, ballCenter.z - center.z);
        if (normal.lengthSqr() < 1.0E-6) {
            normal = velocity.reverse();
        }
        if (normal.lengthSqr() < 1.0E-9) {
            return Vec3.ZERO;
        }
        normal = normal.normalize();
        double closing = -velocity.dot(normal);
        if (closing <= 0.0) {
            return velocity.scale(0.6);
        }
        Vec3 sliding = velocity.add(normal.scale(closing));
        return sliding.scale(0.6).add(normal.scale(closing * 0.3));
    }
}
