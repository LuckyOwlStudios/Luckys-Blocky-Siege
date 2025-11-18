package net.luckystudios.blocks.custom.shooting.spewer;

import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.entity.custom.ember.Ember;
import net.luckystudios.entity.custom.water_drop.WaterBlob;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.gui.spewer_cannon.SpewerCannonBlockBlockMenu;
import net.luckystudios.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class SpewerCannonBlockEntity extends AbstractShootingAimableBlockEntity {

    // Copied from Mcreator! :)
    private final FluidTank fluidTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
            assert level != null;
            level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
        }
    };

    private int fireCooldown = 0;

    public enum LIQUID_CONTENT {
        NONE,
        WATER,
        LAVA,
        POTION
    }

    public SpewerCannonBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SPEWER_CANNON_BLOCK_ENTITY.get(), pos, blockState);
        this.animationLength = 0.25F;
//        this.pitch = 30F;
    }

    public ItemStack getLiquidStack() {
        return this.getItem(0);
    }

    @Override
    public boolean canShoot() {
        return this.fireCooldown <= 0 && !this.fluidTank.isEmpty();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpewerCannonBlockEntity spewerCannonBlockEntity) {
        extraTick(level, pos, state, spewerCannonBlockEntity);

        handleInv(level, pos, spewerCannonBlockEntity);

        // Decrement cooldown
        if (spewerCannonBlockEntity.fireCooldown > 0) {
            spewerCannonBlockEntity.fireCooldown--;
        }

        if (spewerCannonBlockEntity.animationTime > 0) {
            spewerCannonBlockEntity.animationTime--;
        }

        boolean has_redstone = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());

        // Only fire if redstone is on AND cooldown is 0
        if (has_redstone && spewerCannonBlockEntity.fireCooldown <= 0) {
            spewerCannonBlockEntity.fireCannon(level, pos, spewerCannonBlockEntity);
            spewerCannonBlockEntity.fireCooldown = 5; // Set 5-tick cooldown
        }
    }

    private static void handleInv(Level level, BlockPos pos, SpewerCannonBlockEntity spewerCannonBlockEntity) {
        ItemStack itemStack = spewerCannonBlockEntity.getLiquidStack();
        if (spewerCannonBlockEntity.fluidTank.isEmpty()) {
            if (itemStack.is(ModTags.SPEWER_AMMO)) {
                if (itemStack.getItem() instanceof BucketItem bucketItem) {
                    if (itemStack.is(Items.LAVA_BUCKET)) {
                        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0f, 1.0f);
                    } else {
                        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                    spewerCannonBlockEntity.fluidTank.fill(new FluidStack(bucketItem.content, 1000), IFluidHandler.FluidAction.EXECUTE);
                    spewerCannonBlockEntity.removeItem(0, 1);
                    spewerCannonBlockEntity.setItem(0, bucketItem.getCraftingRemainingItem(itemStack));
                }
            }
        }
        if (itemStack.getItem() instanceof PotionItem && spewerCannonBlockEntity.fluidTank.getFluidAmount() <= 666) {
            // Get the potion contents from the item
            PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (!potionContents.equals(PotionContents.EMPTY)) {
                int amount = spewerCannonBlockEntity.fluidTank.getFluidAmount() == 666 ? 334: 333;
                FluidStack stack = new FluidStack(Fluids.WATER, amount);
                stack.set(DataComponents.POTION_CONTENTS, potionContents);
                spewerCannonBlockEntity.fluidTank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                // Don't forget to consume the potion item
                spewerCannonBlockEntity.removeItem(0, 1);
                spewerCannonBlockEntity.setItem(0, new ItemStack(Items.GLASS_BOTTLE));
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    public Vec3 getSpawnPos(BlockPos pos) {
        return Vec3.atCenterOf(pos).add(getAimVector(this).scale(barrelOffsetDistance()));
    }

    public void fireCannon(Level level, BlockPos pos, SpewerCannonBlockEntity spewerCannonBlockEntity) {
        if (!this.canShoot()) return;
        spewerCannonBlockEntity.animationTime = spewerCannonBlockEntity.animationLength;
        // Normalize direction
        Vec3 direction = getAimVector(spewerCannonBlockEntity);

        // Offset to spawn in front of the cannon
        Vec3 spawnPos = Vec3.atCenterOf(pos).add(direction.scale(barrelOffsetDistance()));

        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                getShootSound(), SoundSource.BLOCKS, 0.25f, 0.75f);

        // Create and launch the projectile
        Projectile projectile;
        Fluid fluid = spewerCannonBlockEntity.getFluid();
        if (fluid == Fluids.LAVA) {
            projectile = new Ember(level, spawnPos.x, spawnPos.y, spawnPos.z);
        } else if (fluid == Fluids.WATER) {
            projectile = new WaterBlob(level, spawnPos.x, spawnPos.y, spawnPos.z);
        } else {
            projectile = new Snowball(level, spawnPos.x, spawnPos.y, spawnPos.z);
        }
        projectile.shoot(direction.x, direction.y, direction.z, 0.5F, 12.0F); // power * speed factor
        level.addFreshEntity(projectile);
        spewerCannonBlockEntity.fluidTank.drain(new FluidStack(spewerCannonBlockEntity.getFluidTank().getFluid().getFluid(), 10), IFluidHandler.FluidAction.EXECUTE);
    }

    private SoundEvent getShootSound() {
        LIQUID_CONTENT liquidContent = this.getLiquidContent();
        return switch (liquidContent) {
            case LAVA -> SoundEvents.BLAZE_SHOOT;
            default -> SoundEvents.BUCKET_EMPTY;
        };
    }

    @Override
    public float maxPitch() {
        return 30;
    }

    @Override
    public float minPitch() {
        return 0;
    }

    @Override
    public float pitchOffset() {
        return 30.0F;
    }

    @Override
    public float barrelOffsetDistance() {
        return 1.05F;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.blockysiege.spewer_cannon");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new SpewerCannonBlockBlockMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Needed for liquid
        if (tag.get("fluidTank") instanceof CompoundTag compoundTag)
            fluidTank.readFromNBT(registries, compoundTag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Needed for liquid
        tag.put("fluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
    }

    public FluidTank getFluidTank() {
        return this.fluidTank;
    }

    public Fluid getFluid() {
        return this.fluidTank.getFluid().getFluid();
    }

    public float getFillPercentage() {
        int capacity = fluidTank.getCapacity();
        int amount = fluidTank.getFluidAmount();
        return (float) amount / capacity;
    }

    public LIQUID_CONTENT getLiquidContent() {
        Fluid fluid = fluidTank.getFluid().getFluid();
        if (fluid.isSame(Fluids.LAVA)) {
            return LIQUID_CONTENT.LAVA;
        } else if (fluid.isSame(Fluids.WATER)) {
            PotionContents potionContents = fluidTank.getFluid().get(DataComponents.POTION_CONTENTS);
            if (potionContents != null) {
                return LIQUID_CONTENT.POTION;
            } else {
                return LIQUID_CONTENT.WATER;
            }
        }
        return LIQUID_CONTENT.NONE;
    }

    public PotionContents getEffects() {
        PotionContents potionContents = fluidTank.getFluid().get(DataComponents.POTION_CONTENTS);
        return potionContents;
    }
}
