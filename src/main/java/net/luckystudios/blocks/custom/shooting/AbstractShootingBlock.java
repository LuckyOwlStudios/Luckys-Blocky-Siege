package net.luckystudios.blocks.custom.shooting;

import net.luckystudios.blocks.util.ModBlockStateProperties;
import net.luckystudios.blocks.util.enums.DamageState;
import net.luckystudios.blocks.util.enums.FiringState;
import net.luckystudios.blocks.util.interfaces.DamageableBlock;
import net.luckystudios.entity.custom.seat.Seat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractShootingBlock extends AbstractAimableBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected AbstractShootingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof AbstractShootingAimableBlockEntity)) return 0;
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof AbstractShootingAimableBlockEntity tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        boolean hasPower = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
        boolean isPowered = state.getValue(LIT);
        if (!(level.getBlockEntity(pos) instanceof AbstractShootingAimableBlockEntity shootingAimableBlockEntity)) return;
        boolean canShoot = shootingAimableBlockEntity.canShoot();
        if (hasPower && canShoot && !isPowered) {
            triggerBlock(state, level, pos);
            level.setBlock(pos, state.setValue(LIT, true), 3);
        } else if (!hasPower && isPowered) {
            level.setBlock(pos, state.setValue(LIT, false), 3);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getBlock() instanceof DamageableBlock damageableBlock) {
            Item repairStack = damageableBlock.repairItem();
            if (stack.is(repairStack) && state.getValue(ModBlockStateProperties.DAMAGE_STATE) != DamageState.NONE) {
                damageableBlock.repairBlock(player, level, pos, state);
                return ItemInteractionResult.SUCCESS;
            }
        }

        if (level.getBlockEntity(pos) instanceof AbstractShootingAimableBlockEntity abstractShootingAimableBlockEntity) {
            boolean isHoldingFlintAndSteel = stack.is(Items.FLINT_AND_STEEL);
            boolean canShoot = abstractShootingAimableBlockEntity.canShoot();
            if (isHoldingFlintAndSteel && canShoot) {
                triggerBlock(state, level, pos);
                return ItemInteractionResult.SUCCESS;
            } else if (!(player.getVehicle() instanceof Seat)) {
                player.openMenu(new SimpleMenuProvider(abstractShootingAimableBlockEntity, abstractShootingAimableBlockEntity.getDisplayName()), pos);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
        super.onBlockStateChange(level, pos, oldState, newState);
        if (level.getBlockEntity(pos) instanceof AbstractShootingAimableBlockEntity shootingAimableBlockEntity) {
            if (newState.getValue(LIT) && (newState.hasProperty(ModBlockStateProperties.FIRING_STATE) && newState.getValue(ModBlockStateProperties.FIRING_STATE) == FiringState.OFF) && shootingAimableBlockEntity.canShoot()) {
                triggerBlock(newState, shootingAimableBlockEntity.getLevel(), pos);
            }
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public abstract void triggerBlock(BlockState state, Level level, BlockPos pos);
}
