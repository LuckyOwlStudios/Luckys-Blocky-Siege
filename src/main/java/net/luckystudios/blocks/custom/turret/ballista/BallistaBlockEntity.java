package net.luckystudios.blocks.custom.turret.ballista;

import net.luckystudios.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BallistaBlockEntity extends BlockEntity {
    public BallistaBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.BALLISTA_BLOCK_ENTITY.get(), pos, blockState);
    }
}
