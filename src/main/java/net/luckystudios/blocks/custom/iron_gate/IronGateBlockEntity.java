package net.luckystudios.blocks.custom.iron_gate;

import net.luckystudios.blocks.util.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IronGateBlockEntity extends BlockEntity {

    public IronGateBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.IRON_GATE.get(), pos, blockState);
    }
}
