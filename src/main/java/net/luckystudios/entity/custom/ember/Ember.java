package net.luckystudios.entity.custom.ember;

import net.luckystudios.entity.ImprovedProjectile;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.init.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class Ember extends ImprovedProjectile {

    public Ember(EntityType<? extends Ember> entityType, Level level) {
        super(entityType, level);
    }

    public Ember(Level level, double x, double y, double z) {
        super(ModEntityTypes.EMBER.get(), x, y, z, level);
        this.setRemainingFireTicks(100);
    }

    public Ember(LivingEntity shooter, Level level) {
        super(ModEntityTypes.EMBER.get(), shooter, level);
        this.setRemainingFireTicks(100);
    }

    @Override
    public void tick() {
        super.tick();
        if (isInWater()) {
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
    }

    @Override
    protected List<ParticleOptions> getTrailParticles() {
        return List.of(
                ParticleTypes.SMOKE,
                ModParticleTypes.FLAME_TRAIL.get()
        );
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) return;
        Level level = this.level();
        BlockPos blockPos = result.getBlockPos();
        if (level.getBlockState(blockPos).canBeReplaced()) {
            level.setBlock(blockPos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        entity.igniteForSeconds(5.0F);
        DamageSource damagesource = this.damageSources().inFire();
        entity.hurt(damagesource, 4);
    }
}
