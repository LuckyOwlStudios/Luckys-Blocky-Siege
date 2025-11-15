package net.luckystudios.blocks.custom.shooting.multi_cannon;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.blocks.util.ModBlockStateProperties;
import net.luckystudios.blocks.util.enums.DamageState;
import net.luckystudios.blocks.util.interfaces.DamageableBlock;
import net.luckystudios.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiCannonBlock extends AbstractShootingBlock implements DamageableBlock {
    public static final MapCodec<MultiCannonBlock> CODEC = simpleCodec(MultiCannonBlock::new);
    public static final EnumProperty<DamageState> DAMAGE_STATE = ModBlockStateProperties.DAMAGE_STATE;
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DAMAGE_STATE);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), MultiCannonBlockEntity::tick);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("block.blockysiege.multi_cannon.description"));
    }

    @Override
    public void triggerBlock(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof MultiCannonBlockEntity multiCannonBlockEntity)) return;
        if (multiCannonBlockEntity.cooldown > 0) return;
        multiCannonBlockEntity.cooldown = multiCannonBlockEntity.maxCooldown; // Set fuse time for the cannon
        multiCannonBlockEntity.animationTime = multiCannonBlockEntity.animationLength;
        multiCannonBlockEntity.setChanged();
    }

    @Override
    public Item repairItem() {
        return Items.IRON_INGOT;
    }
}
