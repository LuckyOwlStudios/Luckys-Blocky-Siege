package net.luckystudios.entity.custom.water_drop;

import net.luckystudios.entity.ImprovedProjectile;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.init.ModParticleTypes;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class WaterBlob extends ImprovedProjectile {

    public WaterBlob(EntityType<? extends ImprovedProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public WaterBlob(Level level, double x, double y, double z) {
        super(ModEntityTypes.WATER_BLOB.get(), x, y, z, level);
    }

    public WaterBlob(LivingEntity shooter, Level level) {
        super(ModEntityTypes.WATER_BLOB.get(), shooter, level);
    }

    @Override
    public void tick() {
        super.tick();

        // Despawn if we fall into water
        if (isInWater()) {
            this.discard();
        }

        if (isInLava() || isOnFire()) {
            playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 1.0F);
            if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        this.getX(), this.getY() + 1, this.getZ(),
                        10,  // particle count
                        0.25, 0.25, 0.25,  // spread (x, y, z)
                        0.05  // speed
                );
            }
            this.discard();
        }

        int color = BiomeColors.getAverageWaterColor(level(), this.blockPosition());
        level().addParticle(ColorParticleOption.create(ModParticleTypes.WATER_TRAIL.get(), color), this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        level().addParticle(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
    }

    @Override
    protected List<ParticleOptions> getTrailParticles() {
        return List.of();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide()) return; // Changed to server-side only

        Level level = this.level();
        BlockPos hitPos = result.getBlockPos();
        Direction offset = result.getDirection();
        BlockPos offsetPos = hitPos.relative(offset);

        // Loop through 3x3 area around the center position
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos targetPos = hitPos.offset(x, 0, z);
                BlockState blockState = level.getBlockState(targetPos);

                // Extinguish fire blocks
                if (blockState.is(Blocks.FIRE)) {
                    level.removeBlock(targetPos, false);
                    level.playSound(null, targetPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
                    this.discard();
                }

                // Moisturize farmland
                else if (blockState.hasProperty(BlockStateProperties.MOISTURE)) {
                    level.setBlock(targetPos, blockState.setValue(BlockStateProperties.MOISTURE, 7), 3);
                    this.discard();
                }
            }
        }

        if (level.getBlockState(offsetPos).canBeReplaced()) {
            level.setBlock(offsetPos, Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 8), 3);
            if (level.getBlockState(offsetPos.north()).canBeReplaced() && Blocks.WATER.defaultBlockState().canSurvive(level, offsetPos.north())) {
                level.setBlock(offsetPos.north(), Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 4), 3);
            }
            if (level.getBlockState(offsetPos.east()).canBeReplaced() && Blocks.WATER.defaultBlockState().canSurvive(level, offsetPos.east())) {
                level.setBlock(offsetPos.east(), Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 4), 3);
            }
            if (level.getBlockState(offsetPos.south()).canBeReplaced() && Blocks.WATER.defaultBlockState().canSurvive(level, offsetPos.south())) {
                level.setBlock(offsetPos.south(), Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 4), 3);
            }
            if (level.getBlockState(offsetPos.west()).canBeReplaced() && Blocks.WATER.defaultBlockState().canSurvive(level, offsetPos.west())) {
                level.setBlock(offsetPos.west(), Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 4), 3);
            }
        }

        level.playSound(null, hitPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.discard(); // Remove the projectile after impact
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity.isOnFire()) {
            entity.extinguishFire();
        }
    }
}
