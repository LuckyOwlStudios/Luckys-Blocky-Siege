package net.luckystudios.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class ParticleHandler {

    /**
     * Creates a spherical explosion of cloud particles
     * @param serverLevel The ServerLevel
     * @param particleType Particle to spawn
     * @param centerX Explosion center X coordinate
     * @param centerY Explosion center Y coordinate
     * @param centerZ Explosion center Z coordinate
     * @param particleCount Number of particles to spawn
     * @param speed Speed of particle movement
     */
    public static void spawnParticleRing(ServerLevel serverLevel, SimpleParticleType particleType, double centerX, double centerY, double centerZ, int particleCount, double speed) {
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double xSpeed = Math.cos(angle) * speed;
            double zSpeed = Math.sin(angle) * speed;

            serverLevel.sendParticles(
                    particleType,
                    centerX, centerY, centerZ,
                    0, // Number of particles (0 to spawn one with velocity)
                    xSpeed, 0.0, zSpeed,
                    0.5
            );
        }
    }

    /**
     * Creates a spherical explosion of particles spreading outward in all directions
     * @param serverLevel The ServerLevel
     * @param particleType Particle to spawn
     * @param centerX Explosion center X coordinate
     * @param centerY Explosion center Y coordinate
     * @param centerZ Explosion center Z coordinate
     * @param particleCount Number of particles to spawn
     * @param speed Speed of particle movement
     */
    public static void spawnParticleSphere(ServerLevel serverLevel, ParticleOptions particleType, double centerX, double centerY, double centerZ, int particleCount, double speed) {
        for (int i = 0; i < particleCount; i++) {
            // Generate random point on sphere surface using spherical coordinates
            double theta = Math.random() * 2 * Math.PI; // Azimuthal angle (0 to 2π)
            double phi = Math.acos(2 * Math.random() - 1); // Polar angle (0 to π) with uniform distribution

            // Convert spherical to cartesian coordinates for velocity direction
            double sinPhi = Math.sin(phi);
            double xSpeed = sinPhi * Math.cos(theta) * speed;
            double ySpeed = Math.cos(phi) * speed;
            double zSpeed = sinPhi * Math.sin(theta) * speed;

            serverLevel.sendParticles(
                    particleType,
                    centerX, centerY, centerZ,
                    0, // Number of particles (0 to spawn one with velocity)
                    xSpeed, ySpeed, zSpeed,
                    0.5
            );
        }
    }

    /**
     * Creates a spherical explosion of cloud particles
     * @param level The world level
     * @param centerX Explosion center X coordinate
     * @param centerY Explosion center Y coordinate
     * @param centerZ Explosion center Z coordinate
     * @param particleCount Number of particles to spawn
     * @param spreadRadius How far particles spread from center
     * @param speedMultiplier Speed of particle movement
     * @param random Random source for variation
     */
    public static void createCloudExplosion(Level level, double centerX, double centerY, double centerZ,
                                            int particleCount, double spreadRadius, double speedMultiplier,
                                            RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < particleCount; i++) {
            // Generate random point on sphere surface using spherical coordinates
            double theta = random.nextDouble() * 2 * Math.PI; // Azimuthal angle (0 to 2π)
            double phi = Math.acos(2 * random.nextDouble() - 1); // Polar angle (0 to π) with uniform distribution

            // Convert spherical to cartesian coordinates
            double sinPhi = Math.sin(phi);
            double x = sinPhi * Math.cos(theta);
            double y = Math.cos(phi);
            double z = sinPhi * Math.sin(theta);

            // Add some randomness to the radius for a more natural look
            double radiusVariation = 0.7 + (random.nextDouble() * 0.6); // 0.7 to 1.3
            double radius = spreadRadius * radiusVariation;

            // Calculate final position
            double particleX = centerX + (x * radius);
            double particleY = centerY + (y * radius);
            double particleZ = centerZ + (z * radius);

            // Calculate velocity (particles move outward from center)
            double velocityX = x * speedMultiplier * (0.8 + random.nextDouble() * 0.4);
            double velocityY = y * speedMultiplier * (0.8 + random.nextDouble() * 0.4);
            double velocityZ = z * speedMultiplier * (0.8 + random.nextDouble() * 0.4);

            // Spawn different types of cloud particles for variety
            ParticleOptions particleType = getRandomCloudParticle(random);

            // Use addParticle for server-side spawning that gets sent to clients
            serverLevel.addParticle(
                    particleType,
                    particleX, particleY, particleZ,
                    velocityX, velocityY, velocityZ
            );
        }
    }

    /**
     * Creates a ring of particles around a center point
     * @param level The world level
     * @param centerX Ring center X coordinate
     * @param centerY Ring center Y coordinate
     * @param centerZ Ring center Z coordinate
     * @param ringRadius Radius of the ring
     * @param particleCount Number of particles in the ring
     * @param particleType Type of particle to spawn
     * @param outwardSpeed Speed particles move outward
     * @param random Random source for variation
     */
    public static void createParticleRing(Level level, double centerX, double centerY, double centerZ,
                                          double ringRadius, int particleCount, ParticleOptions particleType,
                                          double outwardSpeed, RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < particleCount; i++) {
            double angle = (i / (double) particleCount) * 2 * Math.PI;

            double ringX = centerX + Math.cos(angle) * ringRadius;
            double ringZ = centerZ + Math.sin(angle) * ringRadius;
            double ringY = centerY + (random.nextGaussian() * 0.3);

            // Ring particles move outward horizontally
            double velocityX = Math.cos(angle) * outwardSpeed;
            double velocityZ = Math.sin(angle) * outwardSpeed;
            double velocityY = random.nextGaussian() * 0.1;

            serverLevel.sendParticles(
                    particleType,
                    ringX, ringY, ringZ,
                    0, // count = 0 to use velocity
                    velocityX, velocityY, velocityZ,
                    1.0 // speed
            );
        }
    }

    /**
     * Creates upward-shooting particles for mushroom cloud effect
     * @param level The world level
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param centerZ Center Z coordinate
     * @param particleCount Number of particles to spawn
     * @param upwardSpeed Base upward velocity
     * @param spread Horizontal spread of particles
     * @param particleType Type of particle to spawn
     * @param random Random source for variation
     */
    public static void createMushroomCloud(Level level, double centerX, double centerY, double centerZ,
                                           int particleCount, double upwardSpeed, double spread,
                                           ParticleOptions particleType, RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < particleCount; i++) {
            double offsetX = random.nextGaussian() * spread;
            double offsetZ = random.nextGaussian() * spread;
            double velocityY = upwardSpeed + random.nextDouble() * (upwardSpeed * 0.5);

            serverLevel.sendParticles(
                    particleType,
                    centerX + offsetX, centerY, centerZ + offsetZ,
                    0, // count = 0 to use velocity
                    random.nextGaussian() * 0.05, velocityY, random.nextGaussian() * 0.05,
                    1.0 // speed
            );
        }
    }

    /**
     * Creates swirling particles around a center point
     * @param level The world level
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param centerZ Center Z coordinate
     * @param particleCount Number of particles to spawn
     * @param maxRadius Maximum spiral radius
     * @param heightRange Vertical range of particles
     * @param swirlingSpeed Speed of swirling motion
     * @param particleType Type of particle to spawn
     * @param random Random source for variation
     */
    public static void createSwirlEffect(Level level, double centerX, double centerY, double centerZ,
                                         int particleCount, double maxRadius, double heightRange,
                                         double swirlingSpeed, ParticleOptions particleType, RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < particleCount; i++) {
            double spiralAngle = random.nextDouble() * 2 * Math.PI;
            double spiralRadius = random.nextDouble() * maxRadius;
            double spiralHeight = random.nextDouble() * heightRange - (heightRange * 0.5);

            double spiralX = centerX + Math.cos(spiralAngle) * spiralRadius;
            double spiralY = centerY + spiralHeight;
            double spiralZ = centerZ + Math.sin(spiralAngle) * spiralRadius;

            // Swirling motion
            double swirlingVelX = -Math.sin(spiralAngle) * swirlingSpeed;
            double swirlingVelZ = Math.cos(spiralAngle) * swirlingSpeed;

            serverLevel.sendParticles(
                    particleType,
                    spiralX, spiralY, spiralZ,
                    0, // count = 0 to use velocity
                    swirlingVelX, random.nextGaussian() * 0.05, swirlingVelZ,
                    1.0 // speed
            );
        }
    }

    /**
     * Creates the complete wind bomb particle effect with all sub-effects
     * @param level The world level
     * @param centerX Explosion center X coordinate
     * @param centerY Explosion center Y coordinate
     * @param centerZ Explosion center Z coordinate
     * @param random Random source for variation
     */
    public static void createWindBombExplosion(Level level, double centerX, double centerY, double centerZ, RandomSource random) {
        // Main spherical cloud explosion
        createCloudExplosion(level, centerX, centerY, centerZ, 80, 4.0, 0.3, random);

        // Ring effect
        createParticleRing(level, centerX, centerY, centerZ, 2.5, 24, ParticleTypes.CLOUD, 0.4, random);

        // Mushroom cloud
        createMushroomCloud(level, centerX, centerY, centerZ, 15, 0.3, 0.5, ParticleTypes.LARGE_SMOKE, random);

        // Swirling effect
        createSwirlEffect(level, centerX, centerY, centerZ, 20, 1.5, 2.0, 0.2, ParticleTypes.CLOUD, random);
    }

    /**
     * Creates a simple explosion particle burst
     * @param level The world level
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param particleCount Number of particles
     * @param spread Spread radius
     * @param particleType Type of particle
     * @param random Random source
     */
    public static void createSimpleExplosion(Level level, double x, double y, double z, int particleCount,
                                             double spread, ParticleOptions particleType, RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < particleCount; i++) {
            double offsetX = random.nextGaussian() * spread;
            double offsetY = random.nextGaussian() * spread;
            double offsetZ = random.nextGaussian() * spread;

            double velocityX = random.nextGaussian() * 0.1;
            double velocityY = random.nextGaussian() * 0.1;
            double velocityZ = random.nextGaussian() * 0.1;

            serverLevel.addParticle(
                    particleType,
                    x + offsetX, y + offsetY, z + offsetZ,
                    velocityX, velocityY, velocityZ
            );
        }
    }

    /**
     * Gets a random cloud-type particle for variety
     * @param random Random source
     * @return Random cloud particle type
     */
    private static ParticleOptions getRandomCloudParticle(RandomSource random) {
        double rand = random.nextDouble();
        if (rand < 0.4) {
            return ParticleTypes.CLOUD;
        } else if (rand < 0.7) {
            return ParticleTypes.WHITE_ASH;
        } else if (rand < 0.9) {
            return ParticleTypes.POOF;
        } else {
            return ParticleTypes.EXPLOSION;
        }
    }

    /**
     * Gets a random explosive particle for variety
     * @param random Random source
     * @return Random explosive particle type
     */
    public static ParticleOptions getRandomExplosiveParticle(RandomSource random) {
        double rand = random.nextDouble();
        if (rand < 0.5) {
            return ParticleTypes.EXPLOSION;
        } else if (rand < 0.8) {
            return ParticleTypes.FLAME;
        } else {
            return ParticleTypes.SMOKE;
        }
    }
}