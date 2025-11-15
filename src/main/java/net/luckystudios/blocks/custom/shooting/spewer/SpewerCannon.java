package net.luckystudios.blocks.custom.shooting.spewer;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.blocks.util.interfaces.DamageableBlock;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.init.ModFluids;
import net.luckystudios.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
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

public class SpewerCannon extends AbstractShootingBlock implements DamageableBlock {
    public static final MapCodec<SpewerCannon> CODEC = simpleCodec(SpewerCannon::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public SpewerCannon(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public void triggerBlock(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof SpewerCannonBlockEntity spewerCannonBlockEntity)) return;
        spewerCannonBlockEntity.fireCannon(level, pos, spewerCannonBlockEntity);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpewerCannonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.SPEWER_CANNON_BLOCK_ENTITY.get(), SpewerCannonBlockEntity::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SpewerCannonBlockEntity spewerCannonBlockEntity) {

            // Only allow interaction with buckets that are in the spewer ammo tag
            if (stack.is(ModTags.SPEWER_AMMO) && stack.getItem() instanceof BucketItem bucketItem) {

                // Check if tank is empty or can accept this fluid
                FluidStack bucketFluid = new FluidStack(bucketItem.content, 1000);
                int amountFilled = spewerCannonBlockEntity.getFluidTank().fill(bucketFluid, IFluidHandler.FluidAction.SIMULATE);

                if (amountFilled > 0) {
                    // Actually fill the tank
                    spewerCannonBlockEntity.getFluidTank().fill(bucketFluid, IFluidHandler.FluidAction.EXECUTE);

                    // Play appropriate sound
                    SoundEvent fillSound = stack.is(Items.LAVA_BUCKET) ?
                            SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
                    level.playSound(null, pos, fillSound, SoundSource.BLOCKS, 1.0f, 1.0f);

                    // Replace bucket with empty bucket (only in survival mode)
                    if (!player.getAbilities().instabuild) {
                        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                        if (stack.getCount() == 1) {
                            player.setItemInHand(hand, emptyBucket);
                        } else {
                            stack.shrink(1);
                            if (!player.getInventory().add(emptyBucket)) {
                                player.drop(emptyBucket, false);
                            }
                        }
                    }

                    return ItemInteractionResult.SUCCESS;
                }
            }

            // If no bucket interaction happened, try opening the GUI
            if (!player.isCrouching()) {
                player.openMenu(spewerCannonBlockEntity, pos);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!(level.getBlockEntity(pos) instanceof SpewerCannonBlockEntity spewerCannonBlockEntity)) return;

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
    public Item repairItem() {
        return Items.IRON_INGOT;
    }
}
