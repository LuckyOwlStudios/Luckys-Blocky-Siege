package net.luckystudios.blocks.custom.shooting.cannon;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.blocks.util.ModBlockStateProperties;
import net.luckystudios.blocks.util.enums.DamageState;
import net.luckystudios.blocks.util.enums.FiringState;
import net.luckystudios.init.ModBlockEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class CannonBlock extends AbstractShootingBlock {
    public static final MapCodec<CannonBlock> CODEC = simpleCodec(CannonBlock::new);
    public static final EnumProperty<FiringState> FIRING_STATE = ModBlockStateProperties.FIRING_STATE;

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public CannonBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE)
                .setValue(DAMAGE_STATE, DamageState.NONE)
                .setValue(FIRING_STATE, FiringState.OFF)
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CannonBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FIRING_STATE);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), CannonBlockEntity::tick);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("block.blockysiege.cannon.description").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void triggerBlock(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof CannonBlockEntity cannonBlockEntity)) return;
        if (cannonBlockEntity.cooldown > -1) return;
        Random random = new Random();
        float f1 = 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F;
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 0.3F, f1);
        level.setBlock(pos, state.setValue(FIRING_STATE, FiringState.CHARGING), 3);
        cannonBlockEntity.cooldown = cannonBlockEntity.maxCooldown; // Set fuse time for the cannon
        cannonBlockEntity.setChanged();
    }

    @Override
    public Item repairItem() {
        return Items.IRON_INGOT;
    }
}
