package net.luckystudios.blocks.custom.explosive_barrel;

import net.luckystudios.entity.custom.explosive_barrel.PrimedExplosiveBarrel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ExplosiveBarrelBlock extends TntBlock {

    public ExplosiveBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide) {
            BlockPos pos = hit.getBlockPos();

            // Create and spawn the primed explosive barrel
            createPrimedBarrel(level, pos, projectile.getOwner() instanceof LivingEntity ? (LivingEntity) projectile.getOwner() : null);

            // Remove the block immediately
            level.removeBlock(pos, false);

            // Play ignition sound and trigger game event
            level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(projectile, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            createPrimedBarrel(level, pos, explosion.getIndirectSourceEntity());
        }
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        if (!level.isClientSide) {
            createPrimedBarrel(level, pos, igniter);

            // Play ignition sound and trigger game event
            level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }

    /**
     * Creates and spawns a primed explosive barrel entity at the given position
     * with randomized fuse time.
     *
     * @param level The level to spawn the entity in
     * @param pos The position where the barrel was located
     * @param igniter The entity that caused the barrel to be primed (can be null)
     */
    private void createPrimedBarrel(Level level, BlockPos pos, @Nullable LivingEntity igniter) {
        // Create the primed barrel entity at the center of the block
        PrimedExplosiveBarrel primedBarrel = new PrimedExplosiveBarrel(
                level,
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                igniter
        );

        // Randomize the fuse time to add unpredictability
        int baseFuse = primedBarrel.getFuse();
        int randomizedFuse = level.random.nextInt(baseFuse / 4) + baseFuse / 8;
        primedBarrel.setFuse((short) randomizedFuse);

        // Spawn the entity in the world
        level.addFreshEntity(primedBarrel);
    }
}