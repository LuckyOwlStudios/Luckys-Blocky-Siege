package net.luckystudios.blocks.custom.shooting.multi_cannon;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.blocks.custom.shooting.cannon.CannonBlockEntity;
import net.luckystudios.blocks.util.ModBlockStateProperties;
import net.luckystudios.blocks.util.enums.DamageState;
import net.luckystudios.blocks.util.interfaces.DamageableBlock;
import net.luckystudios.entity.custom.bullet.FireworkStarProjectile;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.init.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class MultiCannonBlock extends AbstractShootingBlock implements DamageableBlock {
    public static final MapCodec<MultiCannonBlock> CODEC = simpleCodec(MultiCannonBlock::new);
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public MultiCannonBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE)
                .setValue(DAMAGE_STATE, DamageState.NONE)
                .setValue(TRIGGERED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TRIGGERED);
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("block.blockysiege.multi_cannon.description").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void triggerBlock(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof MultiCannonBlockEntity multiCannonBlockEntity)) return;
        if (multiCannonBlockEntity.cooldown > 0) return;

        // Calculate cooldown based on bullet count (10 ticks per bullet)
        level.setBlock(pos, state.setValue(TRIGGERED, true), 3);
        multiCannonBlockEntity.beginFiring(level, pos, multiCannonBlockEntity);
    }

    @Override
    public Ingredient repairItem() {
        return Ingredient.of(Items.IRON_INGOT);
    }
}
