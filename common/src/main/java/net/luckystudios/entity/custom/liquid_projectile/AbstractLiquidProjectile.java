package net.luckystudios.entity.custom.liquid_projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class AbstractLiquidProjectile extends ThrowableItemProjectile {
    public AbstractLiquidProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public AbstractLiquidProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level, double x, double y, double z) {
        super(entityType, x, y, z, level);
    }

    public AbstractLiquidProjectile(EntityType<? extends ThrowableItemProjectile> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.BARRIER;
    }

    protected abstract List<ParticleOptions> getTrailParticles();

    protected abstract SoundEvent impactSound();

    @Nullable
    protected abstract BlockState blockStateToPlace();

    @Override
    public void tick() {
        super.tick();
        for (ParticleOptions particle : getTrailParticles()) {
            this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), -this.getDeltaMovement().x / 4, -this.getDeltaMovement().y / 4, -this.getDeltaMovement().z / 4);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), 8.0F);

        // Try to place block at projectile's current position when hitting entity
        BlockPos projectilePos = this.blockPosition();
        land(level(), projectilePos);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        // Get the position of the block that was hit
        BlockPos hitBlockPos = result.getBlockPos();

        // Get the direction of the face that was hit
        // This tells us which side of the block was hit
        BlockPos placementPos = hitBlockPos.relative(result.getDirection());
        land(level(), placementPos);
        this.discard();
    }

    public void land(Level level, BlockPos placementPos) {
        if (blockStateToPlace() == null) return;
        BlockState currentState = level.getBlockState(placementPos);
        if (currentState.canBeReplaced()) {
            level().setBlock(placementPos, Objects.requireNonNull(blockStateToPlace()), 3);
        }
        this.level().playSound(this, this.blockPosition(), impactSound(), SoundSource.BLOCKS, 0.5F, 1.0F);
        this.level().broadcastEntityEvent(this, (byte)3);
    }
}