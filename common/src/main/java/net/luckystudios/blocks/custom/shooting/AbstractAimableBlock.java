package net.luckystudios.blocks.custom.shooting;

import net.luckystudios.blocks.custom.shooting.spewer.SpewerBlockEntity;
import net.luckystudios.blocks.custom.shooting.volley.VolleyRackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAimableBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected AbstractAimableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide && placer != null && level.getBlockEntity(pos) instanceof AbstractShootingAimableBlockEntity aimableBlockEntity) {
            aimableBlockEntity.yaw = Direction.fromYRot(placer.getYRot()).toYRot();

            // Set initial pitch for spewer cannons
            if (aimableBlockEntity instanceof SpewerBlockEntity || aimableBlockEntity instanceof VolleyRackBlockEntity) {
                aimableBlockEntity.setPitch(30.0F); // Use setPitch to ensure proper clamping
            }
        }
    }
}
