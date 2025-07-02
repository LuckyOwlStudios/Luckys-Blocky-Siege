package net.luckystudios.blocks.custom.cannon.types.generic;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.cannon.AbstractAimableBlock;
import net.luckystudios.blocks.util.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CannonBlock extends AbstractAimableBlock {
    public static final MapCodec<CannonBlock> CODEC = simpleCodec(CannonBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public CannonBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof CannonBlockEntity cannonBlockEntity) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    triggerBlock(state, level, pos);
                } else {
                    player.openMenu(new SimpleMenuProvider(cannonBlockEntity, Component.translatable("container.blockysiege.cannon")), pos);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CannonBlockEntity(pos, state);
    }

    /**
     * Returns the analog signal this block emits. This is the signal a comparator can read from it.
     *
     */
    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ?
                createTickerHelper(blockEntityType, ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), CannonBlockEntity::clientTick) :
                createTickerHelper(blockEntityType, ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), CannonBlockEntity::tick);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        boolean flag = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
        boolean flag1 = state.getValue(POWERED);
        if (flag && !flag1) {
            System.out.println("RAN");
            triggerBlock(state, level, pos);
            level.setBlock(pos, state.setValue(POWERED, true), 3);
        } else if (!flag && flag1) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
        }
    }

    @Override
    public void triggerBlock(BlockState state, Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (level.getBlockEntity(pos) instanceof CannonBlockEntity cannonBlockEntity) {
            if (cannonBlockEntity.cooldown != 0) return;
            cannonBlockEntity.cooldown = 60; // Set fuse time for the cannon
            cannonBlockEntity.setChanged();
        }
    }
}
