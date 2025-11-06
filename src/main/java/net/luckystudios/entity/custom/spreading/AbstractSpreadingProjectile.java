package net.luckystudios.entity.custom.spreading;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractSpreadingProjectile extends ThrowableItemProjectile {

    public AbstractSpreadingProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public AbstractSpreadingProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level, double x, double y, double z) {
        super(entityType, x, y, z, level);
    }

    public AbstractSpreadingProjectile(EntityType<? extends ThrowableItemProjectile> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.BARRIER;
    }

    protected abstract List<ParticleOptions> getTrailParticles();

    protected abstract SoundEvent impactSound();

    protected abstract Block blockToPlace();

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
        if (level().getBlockState(projectilePos).canBeReplaced()) {
            level().setBlock(projectilePos, blockToPlace().defaultBlockState(), 3);
        }

        land();
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

        System.out.println("Hit block at: " + hitBlockPos + ", trying to place at: " + placementPos);

        // Check if the placement position is valid
        BlockState currentState = level().getBlockState(placementPos);
        if (currentState.canBeReplaced()) {
            level().setBlock(placementPos, blockToPlace().defaultBlockState(), 3);
            System.out.println("Block placed successfully!");
        } else {
            System.out.println("Cannot place block - position occupied by: " + currentState.getBlock().getName().getString());
        }

        land();
        this.discard();
    }

    public void land() {
        this.level().playSound(this, this.blockPosition(), impactSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        this.level().broadcastEntityEvent(this, (byte)3);
    }
}