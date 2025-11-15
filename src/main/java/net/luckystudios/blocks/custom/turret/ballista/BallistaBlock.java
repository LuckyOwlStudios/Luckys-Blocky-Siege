package net.luckystudios.blocks.custom.turret.ballista;

import com.mojang.serialization.MapCodec;
import net.luckystudios.entity.custom.turrets.ballista.BallistaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BallistaBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final MapCodec<BallistaBlock> CODEC = simpleCodec(BallistaBlock::new);

    public BallistaBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BallistaBlockEntity(blockPos, blockState);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (movedByPiston) return;
        BlockPos abovePos = pos.above();
        if (level.getBlockState(abovePos).canBeReplaced()) {
            BallistaEntity ballista = new BallistaEntity(level, pos);
            ballista.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            level.addFreshEntity(ballista);
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos abovePos = context.getClickedPos().above();
        Level level = context.getLevel();
        if (level.getBlockState(abovePos).canBeReplaced()) {
            return super.getStateForPlacement(context);
        }
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof BallistaBlockEntity ballistaBlockEntity) {
            player.openMenu(new SimpleMenuProvider(ballistaBlockEntity, ballistaBlockEntity.getDisplayName()), pos);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // Only remove entity if the block is actually being broken (not just block state changing)
        if (!state.is(newState.getBlock())) {
            // Find and remove the ballista entity above this block
            BlockPos abovePos = pos.above();
            List<BallistaEntity> ballistas = level.getEntitiesOfClass(BallistaEntity.class,
                    new AABB(abovePos).inflate(0.1)); // Small area around the position above

            for (BallistaEntity ballista : ballistas) {
                ballista.discard(); // Remove the entity
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
