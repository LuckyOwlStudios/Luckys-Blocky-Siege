package net.luckystudios.blocks.custom.shooting.volley;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.blocks.custom.shooting.cannon.CannonBlockEntity;
import net.luckystudios.entity.custom.bullet.FireworkStarProjectile;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.init.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class VolleyRackBlock extends AbstractShootingBlock {
    protected static final VoxelShape SHAPE = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)13.0F, (double)16.0F);
    public static final MapCodec<VolleyRackBlock> CODEC = simpleCodec(VolleyRackBlock::new);

    public VolleyRackBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public void triggerBlock(BlockState state, Level level, BlockPos pos) {

    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public Ingredient repairItem() {
        return Ingredient.of(ItemTags.PLANKS);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new VolleyRackBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.VOLLEY_BLOCK_ENTITY.get(), VolleyRackBlockEntity::tick);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("block.blockysiege.volley_rack.description").withStyle(ChatFormatting.GRAY));
    }

    // Override due to this block being weaker, so it breaks in only two hits
    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        super.onProjectileHit(level, state, hit, projectile);
        if (level.isClientSide()) return;
        if (projectile.getType().is(ModTags.STRONG_PROJECTILE)) {
            if (projectile instanceof FireworkStarProjectile) {
                Random random = new Random();
                if (random.nextFloat() < 0.5F) {
                    damageBlockFully(null, level, hit.getBlockPos(), state);
                }
            } else {
                damageBlockFully(null, level, hit.getBlockPos(), state);
            }
        }
    }
}
