package net.luckystudios.blocks.custom.cannon.types.multi;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.cannon.AbstractAimableBlock;
import net.luckystudios.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MultiCannonBlock extends AbstractAimableBlock {
    public static final MapCodec<MultiCannonBlock> CODEC = simpleCodec(MultiCannonBlock::new);
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public MultiCannonBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiCannonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), MultiCannonBlockEntity::tick);
    }

    @Override
    public void triggerBlock(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof MultiCannonBlockEntity multiCannonBlockEntity)) return;
        multiCannonBlockEntity.cooldown = multiCannonBlockEntity.maxCooldown; // Set fuse time for the cannon
        multiCannonBlockEntity.animationTime = multiCannonBlockEntity.animationLength;
        multiCannonBlockEntity.setChanged();
    }
}
