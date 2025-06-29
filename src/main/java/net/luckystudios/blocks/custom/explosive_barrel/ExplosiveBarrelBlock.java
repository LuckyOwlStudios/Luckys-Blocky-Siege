package net.luckystudios.blocks.custom.explosive_barrel;

import net.luckystudios.entity.custom.explosive_barrel.PrimedExplosiveBarrel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ExplosiveBarrelBlock extends TntBlock {

    public ExplosiveBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            PrimedExplosiveBarrel primedExplosiveBarrel = new PrimedExplosiveBarrel(
                    level, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, explosion.getIndirectSourceEntity()
            );
            int i = primedExplosiveBarrel.getFuse();
            primedExplosiveBarrel.setFuse((short)(level.random.nextInt(i / 4) + i / 8));
            level.addFreshEntity(primedExplosiveBarrel);
        }
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        if (!level.isClientSide) {
            PrimedExplosiveBarrel primedExplosiveBarrel = new PrimedExplosiveBarrel(level, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, igniter);
            level.addFreshEntity(primedExplosiveBarrel);
            level.playSound(null, primedExplosiveBarrel.getX(), primedExplosiveBarrel.getY(), primedExplosiveBarrel.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }
}
