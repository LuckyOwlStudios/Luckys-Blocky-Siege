package net.luckystudios.entity.custom.turrets.ballista;

import net.luckystudios.init.ModBlocks;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.entity.custom.turrets.AbstractTurret;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Ballista extends AbstractTurret {

    public Ballista(EntityType<Ballista> entityType, Level level) {
        super(entityType, level);
    }

    public Ballista(Level level, BlockPos pos) {
        super(ModEntityTypes.BALLISTA.get(), level, pos);
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
