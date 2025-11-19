package net.luckystudios.blocks.custom.shooting.spewer;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.blocks.util.interfaces.DamageableBlock;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.init.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpewerBlock extends AbstractShootingBlock implements DamageableBlock {
    public static final MapCodec<SpewerBlock> CODEC = simpleCodec(SpewerBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public SpewerBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public void triggerBlock(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof SpewerBlockEntity spewerCannonBlockEntity)) return;
        spewerCannonBlockEntity.fireSpewer(level, pos, spewerCannonBlockEntity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpewerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.SPEWER_BLOCK_ENTITY.get(), SpewerBlockEntity::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SpewerBlockEntity spewerCannonBlockEntity) {
            // Only allow interaction with buckets that are in the spewer ammo tag
            if (stack.is(ModTags.SPEWER_AMMO)) {
                ItemStack itemStack = spewerCannonBlockEntity.getLiquidStack();
                if (spewerCannonBlockEntity.getFluidTank().getFluidAmount() <= 666) {
                    if (itemStack.getItem() instanceof BucketItem bucketItem) {
                        if (itemStack.is(Items.LAVA_BUCKET)) {
                            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                    SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0f, 1.0f);
                        } else {
                            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                    SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                        }
                        spewerCannonBlockEntity.getFluidTank().fill(new FluidStack(bucketItem.content, 1000), IFluidHandler.FluidAction.EXECUTE);
                        return ItemInteractionResult.SUCCESS;
                    }
                } else if (itemStack.getItem() instanceof PotionItem) {
                    // Get the potion contents from the item
                    PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    if (!potionContents.equals(PotionContents.EMPTY)) {
                        int amount = spewerCannonBlockEntity.getFluidTank().getFluidAmount() == 666 ? 334 : 333;
                        FluidStack stack2 = new FluidStack(Fluids.WATER, amount);
                        stack.set(DataComponents.POTION_CONTENTS, potionContents);
                        spewerCannonBlockEntity.getFluidTank().fill(stack2, IFluidHandler.FluidAction.EXECUTE);
                        // Don't forget to consume the potion item
                        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            } else if (!player.isCrouching()) {
                player.openMenu(spewerCannonBlockEntity, pos);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!(level.getBlockEntity(pos) instanceof SpewerBlockEntity spewerCannonBlockEntity)) return;

        Vec3 spawnPos = spewerCannonBlockEntity.getSpawnPos(pos);
        ParticleOptions particleOptions;
        Fluid fluid = spewerCannonBlockEntity.getFluidTank().getFluid().getFluid();

        if (fluid.isSame(Fluids.EMPTY)) return;

        if (fluid.isSame(Fluids.LAVA)) {
            particleOptions = ParticleTypes.SMOKE;
        } else {
            // Only try to get potion data if it's actually your potion fluid
            FluidStack fluidStack = spewerCannonBlockEntity.getFluidTank().getFluid();
            PotionContents potionContents = fluidStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            // Check if we actually have potion contents and they're not empty
            if (!potionContents.equals(PotionContents.EMPTY)) {
                int color = potionContents.getColor();
                particleOptions = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color);
            } else {
                // Default to a neutral particle for potion fluid without effects
                particleOptions = ParticleTypes.FALLING_WATER;
            }
        }

        level.addAlwaysVisibleParticle(particleOptions, spawnPos.x, spawnPos.y, spawnPos.z, 0.0, 0.0, 0.0);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("block.blockysiege.spewer_cannon.description").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Ingredient repairItem() {
        return Ingredient.of(Items.IRON_INGOT);
    }
}
