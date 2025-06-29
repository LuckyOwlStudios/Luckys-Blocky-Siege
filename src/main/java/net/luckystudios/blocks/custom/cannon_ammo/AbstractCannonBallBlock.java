package net.luckystudios.blocks.custom.cannon_ammo;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

// Based on the candle block, this block can be stacked.
public abstract class AbstractCannonBallBlock extends Block implements SimpleWaterloggedBlock, CannonBallProjectileBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty STACK = IntegerProperty.create("stack", 1, 4);

    @Override
    protected abstract MapCodec<? extends AbstractCannonBallBlock> codec();

    private static final VoxelShape ONE_AABB = Block.box(4, 0, 4, 12, 8, 12);
    private static final VoxelShape FOURTH_AABB = Block.box(4, 6, 4, 12, 16, 12);
    private static final VoxelShape X_TWO_AABB = Shapes.or(
            Block.box(2, 0, 8, 10, 8, 16),
            Block.box(6, 0, 0, 14, 8, 8)
    );
    private static final VoxelShape Z_TWO_AABB = Shapes.or(
            Block.box(0, 0, 2, 8, 8, 10),
            Block.box(8, 0, 6, 16, 8, 14)
    );
    private static final VoxelShape N_THREE_AABB = Shapes.or(
            Block.box(8, 0, 0, 16, 8, 16),
            Block.box(0, 0, 4, 8, 8, 12)
    );
    private static final VoxelShape N_FOUR_AABB = Shapes.or(
            N_THREE_AABB,
            FOURTH_AABB
    );
    private static final VoxelShape E_THREE_AABB = Shapes.or(
            Block.box(0, 0, 8, 16, 8, 16),
            Block.box(4, 0, 0, 12, 8, 8)
    );

    private static final VoxelShape E_FOUR_AABB = Shapes.or(
            E_THREE_AABB,
            FOURTH_AABB
    );
    private static final VoxelShape S_THREE_AABB = Shapes.or(
            Block.box(0, 0, 0, 8, 8, 16),
            Block.box(8, 0, 4, 16, 8, 12)
    );
    private static final VoxelShape S_FOUR_AABB = Shapes.or(
            S_THREE_AABB,
            FOURTH_AABB
    );
    private static final VoxelShape W_THREE_AABB = Shapes.or(
            Block.box(0, 0, 0, 16, 8, 8),
            Block.box(4, 0, 8, 12, 8, 16)
    );

    private static final VoxelShape W_FOUR_AABB = Shapes.or(
            W_THREE_AABB,
            FOURTH_AABB
    );


    public AbstractCannonBallBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(STACK, 1)
                        .setValue(FACING, Direction.NORTH)
                        .setValue(WATERLOGGED, Boolean.FALSE)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STACK, FACING, WATERLOGGED);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return !useContext.isSecondaryUseActive() && useContext.getItemInHand().getItem() == this.asItem() && state.getValue(STACK) < 4 || super.canBeReplaced(state, useContext);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            return blockstate.cycle(STACK);
        } else {
            FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
            boolean flag = fluidstate.getType() == Fluids.WATER;
            return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(WATERLOGGED, flag).setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos
    ) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        switch (state.getValue(STACK)) {
            case 1:
                return ONE_AABB;
            case 2:
                return direction == Direction.WEST || direction == Direction.EAST ? X_TWO_AABB : Z_TWO_AABB;
            case 3:
                switch (direction) {
                    case NORTH -> {
                        return N_THREE_AABB;
                    }
                    case EAST -> {
                        return E_THREE_AABB;
                    }
                    case SOUTH -> {
                        return S_THREE_AABB;
                    }
                    case WEST -> {
                        return W_THREE_AABB;
                    }
                }
            case 4:
                switch (direction) {
                    case NORTH -> {
                        return N_FOUR_AABB;
                    }
                    case EAST -> {
                        return E_FOUR_AABB;
                    }
                    case SOUTH -> {
                        return S_FOUR_AABB;
                    }
                    case WEST -> {
                        return W_FOUR_AABB;
                    }
                }
        }
        return ONE_AABB;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }
}
