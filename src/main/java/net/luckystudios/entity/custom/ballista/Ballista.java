package net.luckystudios.entity.custom.ballista;

import net.luckystudios.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Ballista extends AbstractTurret {

    public Ballista(EntityType<? extends AgeableMob> entityType, Level level) {
        super(entityType, level);
    }

    protected Ballista(EntityType<? extends AgeableMob> entityType, Level level, BlockPos pos) {
        super(entityType, level, pos);
    }

    @Override
    public BlockState attachedBlockState() {
        return ModBlocks.CANNON_BALL.get().defaultBlockState();
    }

    @Override
    public ParticleOptions breakParticle() {
        return new BlockParticleOption(ParticleTypes.BLOCK, attachedBlockState());
    }
}
