package net.luckystudios.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PileBlock extends CarpetBlock {

    public enum PileType {
        FIRE,
        FROST,
        SCULK
    }

    private final PileType pileType;
    private static final int BASE_LIFETIME = 600; // 30 seconds in ticks (20 ticks per second)
    private static final int RANDOM_VARIANCE = 60; // 3 seconds in ticks

    public PileBlock(PileType pileType, Properties properties) {
        super(properties.noCollission().instabreak().noCollission());
        this.pileType = pileType;
    }

    public PileType getPileType() {
        return pileType;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide) {
            // Schedule the block to disappear after a randomized time
            int randomizedLifetime = BASE_LIFETIME + level.getRandom().nextInt(RANDOM_VARIANCE * 2 + 1) - RANDOM_VARIANCE;
            level.scheduleTick(pos, this, randomizedLifetime);

            // Schedule warning particles 60 ticks (3 seconds) before disappearing
            int warningTime = Math.max(1, randomizedLifetime - 60);
            level.scheduleTick(pos, this, warningTime);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        // Check if this is the final tick (block should disappear)
        if (level.getBlockTicks().hasScheduledTick(pos, this)) {
            // Check if there are more ticks scheduled (meaning this is the warning tick)
            boolean hasMoreTicks = level.getBlockTicks().willTickThisTick(pos, this);

            if (!hasMoreTicks) {
                // This is the final tick - remove the block
                spawnFinalDisappearParticles(level, pos);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            } else {
                // This is a warning tick - spawn warning particles
                spawnWarningParticles(level, pos, random);
            }
        }
    }

    private void spawnWarningParticles(ServerLevel level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.1D;
        double z = pos.getZ() + 0.5D;

        switch (pileType) {
            case FIRE:
                // Smoke particles rising up
                for (int i = 0; i < 3; i++) {
                    double offsetX = (random.nextFloat() - 0.5F) * 0.8F;
                    double offsetZ = (random.nextFloat() - 0.5F) * 0.8F;
                    level.sendParticles(ParticleTypes.SMOKE, x + offsetX, y, z + offsetZ, 1, 0, 0.1, 0, 0.01);
                }
                break;
            case FROST:
                // White ash particles dispersing
                for (int i = 0; i < 4; i++) {
                    double offsetX = (random.nextFloat() - 0.5F) * 0.8F;
                    double offsetZ = (random.nextFloat() - 0.5F) * 0.8F;
                    level.sendParticles(ParticleTypes.WHITE_ASH, x + offsetX, y, z + offsetZ, 1, 0, 0.05, 0, 0.02);
                }
                break;
            case SCULK:
                // Soul particles floating away
                for (int i = 0; i < 2; i++) {
                    double offsetX = (random.nextFloat() - 0.5F) * 0.8F;
                    double offsetZ = (random.nextFloat() - 0.5F) * 0.8F;
                    level.sendParticles(ParticleTypes.SOUL, x + offsetX, y + 0.3, z + offsetZ, 1, 0, 0.1, 0, 0.01);
                }
                break;
        }
    }

    private void spawnFinalDisappearParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.1D;
        double z = pos.getZ() + 0.5D;

        switch (pileType) {
            case FIRE:
                // Big puff of smoke
                level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 8, 0.3, 0.1, 0.3, 0.02);
                break;
            case FROST:
                // Snow explosion
                level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 12, 0.4, 0.2, 0.4, 0.05);
                break;
            case SCULK:
                // Soul burst
                level.sendParticles(ParticleTypes.SOUL, x, y + 0.5, z, 6, 0.2, 0.3, 0.2, 0.03);
                break;
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        double randomX = pos.getX() + random.nextFloat();
        double randomZ = pos.getZ() + random.nextFloat();
        double randomY = pos.getY() + random.nextFloat() * 0.5F;

        switch (pileType) {
            case FIRE:
                // Fire pile - lava, smoke, and flame particles
                level.addParticle(ParticleTypes.LAVA, randomX, randomY, randomZ, 0.0F, 0.0F, 0.0F);
                level.addParticle(ParticleTypes.SMOKE, randomX, pos.getY() + random.nextFloat() * 0.8F, randomZ, 0.0F, 0.0F, 0.0F);
                level.addParticle(ParticleTypes.FLAME, randomX, pos.getY() + random.nextFloat() * 0.6F, randomZ, 0.0F, 0.0F, 0.0F);
                break;

            case FROST:
                // Frost pile - snowflake and white ash particles
                level.addParticle(ParticleTypes.SNOWFLAKE, randomX, randomY, randomZ, 0.0F, 0.05F, 0.0F);
                level.addParticle(ParticleTypes.WHITE_ASH, randomX, pos.getY() + random.nextFloat() * 0.4F, randomZ, 0.0F, 0.02F, 0.0F);
                // Occasional puff of cold air
                if (random.nextFloat() < 0.3F) {
                    level.addParticle(ParticleTypes.CLOUD, randomX, pos.getY() + 0.1F, randomZ, 0.0F, 0.0F, 0.0F);
                }
                break;

            case SCULK:
                // Sculk pile - soul fire flame and warped spore particles
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, randomX, randomY, randomZ, 0.0F, 0.0F, 0.0F);
                level.addParticle(ParticleTypes.WARPED_SPORE, randomX, pos.getY() + random.nextFloat() * 0.7F, randomZ,
                        (random.nextFloat() - 0.5F) * 0.02F, random.nextFloat() * 0.01F, (random.nextFloat() - 0.5F) * 0.02F);
                // Occasional soul particle
                if (random.nextFloat() < 0.2F) {
                    level.addParticle(ParticleTypes.SOUL, randomX, pos.getY() + 0.2F, randomZ, 0.0F, 0.01F, 0.0F);
                }
                break;
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        // Only affect entities that aren't already in this block type
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;

            // Apply slowdown effect
            entity.makeStuckInBlock(state, new Vec3(0.9D, 1.5D, 0.9D));

            // Handle client-side particle effects
            if (level.isClientSide) {
                spawnMovementParticles(level, pos, entity);
            }

            // Handle server-side effects
            if (!level.isClientSide) {
                applyPileEffects(level, livingEntity);
            }
        }
    }

    private void spawnMovementParticles(Level level, BlockPos pos, Entity entity) {
        RandomSource random = level.getRandom();
        boolean isMoving = entity.xOld != entity.getX() || entity.zOld != entity.getZ();

        if (isMoving && random.nextFloat() < 0.6F) { // 60% chance instead of 50%
            double particleX = entity.getX() + (random.nextFloat() - 0.5F) * 0.4F;
            double particleY = pos.getY() + 1.0D;
            double particleZ = entity.getZ() + (random.nextFloat() - 0.5F) * 0.4F;

            double velocityX = Mth.randomBetween(random, -1.0F, 1.0F) * 0.1F;
            double velocityZ = Mth.randomBetween(random, -1.0F, 1.0F) * 0.1F;

            switch (pileType) {
                case FIRE:
                    // Multiple fire particles for more dramatic effect
                    level.addParticle(ParticleTypes.LAVA, particleX, particleY, particleZ, velocityX, 0.05D, velocityZ);
                    if (random.nextBoolean()) {
                        level.addParticle(ParticleTypes.FLAME, particleX, particleY - 0.2D, particleZ, velocityX * 0.5F, 0.02D, velocityZ * 0.5F);
                    }
                    break;

                case FROST:
                    // Snow and ice particles
                    level.addParticle(ParticleTypes.SNOWFLAKE, particleX, particleY, particleZ, velocityX, 0.02D, velocityZ);
                    if (random.nextFloat() < 0.3F) {
                        level.addParticle(ParticleTypes.WHITE_ASH, particleX, particleY - 0.1D, particleZ, velocityX * 0.3F, 0.01D, velocityZ * 0.3F);
                    }
                    break;

                case SCULK:
                    // Sculk spores with eerie movement
                    level.addParticle(ParticleTypes.WARPED_SPORE, particleX, particleY, particleZ, velocityX, 0.03D, velocityZ);
                    if (random.nextFloat() < 0.25F) {
                        level.addParticle(ParticleTypes.SOUL, particleX, particleY + 0.2D, particleZ, 0.0D, 0.02D, 0.0D);
                    }
                    break;
            }
        }
    }

    private void applyPileEffects(Level level, LivingEntity entity) {
        switch (pileType) {
            case FIRE:
                // Fire damage with burn effect
                entity.hurt(level.damageSources().hotFloor(), 2.0F); // Reduced from 4.0F
                entity.setRemainingFireTicks(3); // Add burning effect
                break;

            case FROST:
                // Frost effects
                entity.setIsInPowderSnow(true);
                entity.hurt(level.damageSources().freeze(), 1.5F); // Reduced damage
                // Add slowness effect
                if (entity.getRandom().nextFloat() < 0.1F) { // 10% chance per tick
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
                }
                break;

            case SCULK:
                // Sculk effects - darkness and weakness
                if (entity.getRandom().nextFloat() < 0.05F) { // 5% chance per tick
                    entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                }
                // Minor wither damage
                if (entity.getRandom().nextFloat() < 0.02F) { // 2% chance per tick
                    entity.hurt(level.damageSources().wither(), 1.0F);
                }
                break;
        }
    }
}