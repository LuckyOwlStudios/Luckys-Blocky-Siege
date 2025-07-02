package net.luckystudios.blocks.custom.cannon.types.multi;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.cannon.AbstractAimableBlock;
import net.luckystudios.blocks.util.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof MultiCannonBlockEntity multiCannonBlockEntity) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    triggerBlock(state, level, pos);
                } else {
                    player.openMenu(new SimpleMenuProvider(multiCannonBlockEntity, Component.translatable("container.blockysiege.multi_cannon")), pos);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
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
        if (level.getBlockEntity(pos) instanceof MultiCannonBlockEntity cannonBlockEntity) {
            if (cannonBlockEntity.cooldown != 0) return;
            cannonBlockEntity.cooldown = MultiCannonBlockEntity.maxCooldown; // Set fuse time for the cannon
            cannonBlockEntity.setChanged();
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiCannonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ?
                createTickerHelper(blockEntityType, ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), MultiCannonBlockEntity::clientTick) :
                createTickerHelper(blockEntityType, ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), MultiCannonBlockEntity::tick);
    }
}
