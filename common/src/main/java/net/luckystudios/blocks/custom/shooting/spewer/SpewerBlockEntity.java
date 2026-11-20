package net.luckystudios.blocks.custom.shooting.spewer;

import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.entity.custom.ember.Ember;
import net.luckystudios.entity.custom.potion_blob.PotionBlob;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.luckystudios.fluids.FluidStack;
import net.luckystudios.platform.Services;
import net.luckystudios.fluids.FluidTank;
import org.jetbrains.annotations.NotNull;

public class SpewerBlockEntity extends AbstractShootingAimableBlockEntity {

    /** Blocks per tick. 0.9 throws a blob about 20 blocks at full elevation. */
    public static final float MUZZLE_VELOCITY = 0.9F;
    /** Spray inaccuracy, as passed to Projectile#shoot. */
    public static final float SPREAD = 8.0F;

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
    @Override
    public boolean supportsHoldFire() {
        return true;
    }

    public enum LIQUID_CONTENT {
        NONE,
        WATER,
        LAVA,
        POTION
    }

    public SpewerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SPEWER_BLOCK_ENTITY.get(), pos, blockState);
        this.animationLength = 0.25F;
    }

    public ItemStack getLiquidStack() {
        return this.getItem(0);
    }

    @Override
    public boolean canShoot() {
        return this.fireCooldown <= 0 && !this.getShotFluid().isEmpty();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpewerBlockEntity spewerCannonBlockEntity) {
        extraTick(level, pos, state, spewerCannonBlockEntity);

        handleInv(level, pos, spewerCannonBlockEntity);

        // Decrement cooldown
        if (spewerCannonBlockEntity.fireCooldown > 0) {
            spewerCannonBlockEntity.fireCooldown--;
        }

        if (spewerCannonBlockEntity.animationTime > 0) {
            spewerCannonBlockEntity.animationTime--;
        }

        boolean holding = spewerCannonBlockEntity.tickHoldFire();
        boolean has_redstone = holding || level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());

        // Only fire if redstone is on AND cooldown is 0
        if (has_redstone && spewerCannonBlockEntity.fireCooldown <= 0) {
            spewerCannonBlockEntity.fireSpewer(level, pos, spewerCannonBlockEntity);
            spewerCannonBlockEntity.fireCooldown = 5; // Set 5-tick cooldown
        }
    }

    private static void handleInv(Level level, BlockPos pos, SpewerBlockEntity spewer) {
        if (level.isClientSide()) return;
        ItemStack itemStack = spewer.getLiquidStack();
        if (itemStack.isEmpty() || itemStack.getCount() != 1) return;
        FluidTank tank = spewer.fluidTank;

        // Potions and bottles
        if (itemStack.getItem() instanceof PotionItem) {
            if (tank.getFluidAmount() > 666) return;
            PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (potionContents.equals(PotionContents.EMPTY)) return;
            FluidStack stack = new FluidStack(Fluids.WATER, tank.getFluidAmount() == 666 ? 334 : 333);
            stack.set(DataComponents.POTION_CONTENTS, potionContents);
            if (tank.fill(stack, FluidTank.FluidAction.SIMULATE) != stack.getAmount()) return;
            tank.fill(stack, FluidTank.FluidAction.EXECUTE);
            spewer.setItem(0, new ItemStack(Items.GLASS_BOTTLE));
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
            return;
        }
        if (itemStack.is(Items.GLASS_BOTTLE)) {
            PotionContents contents = tank.getFluid().get(DataComponents.POTION_CONTENTS);
            if (contents == null || tank.getFluidAmount() < 333) return;
            int amount = tank.getFluidAmount() <= 334 ? tank.getFluidAmount() : 333;
            tank.drain(amount, FluidTank.FluidAction.EXECUTE);
            ItemStack potion = new ItemStack(Items.POTION);
            potion.set(DataComponents.POTION_CONTENTS, contents);
            spewer.setItem(0, potion);
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            return;
        }

        // Any fluid container item (vanilla or modded)
        if (!Services.PLATFORM.isFluidContainer(itemStack)) return;

        // Container holds fluid: pour it into the tank
        ItemStack result = Services.PLATFORM.emptyContainerInto(itemStack, tank);
        if (result != null) {
            spewer.setItem(0, result);
            playFluidSound(level, pos, tank.getFluid(), true);
            return;
        }

        // Container is empty (or has room): fill it from the tank
        if (tank.isEmpty()) return;
        FluidStack before = tank.getFluid().copy();
        result = Services.PLATFORM.fillContainerFrom(itemStack, tank);
        if (result != null) {
            spewer.setItem(0, result);
            playFluidSound(level, pos, before, false);
        }
    }

    private static void playFluidSound(Level level, BlockPos pos, FluidStack fluid, boolean emptyingIntoTank) {
        if (fluid.isEmpty()) return;
        level.playSound(null, pos, Services.PLATFORM.getFluidSound(fluid, emptyingIntoTank), SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    // Liquid a mob crew brings. Sprayed when the tank is empty, but never put in the tank, so it can't be bottled
    // or bucketed out, and not saved: it only exists while a crew works the spewer.
    private FluidStack crewFluid = FluidStack.EMPTY;

    public void setCrewFluid(FluidStack fluid) {
        this.crewFluid = fluid.copy();
    }

    @Override
    public boolean hasCrewAmmo() {
        return !this.crewFluid.isEmpty();
    }

    @Override
    public void clearCrewAmmo() {
        this.crewFluid = FluidStack.EMPTY;
    }

    /** What the next shot sprays: the tank's contents, else the crew's. */
    public FluidStack getShotFluid() {
        return this.fluidTank.isEmpty() ? this.crewFluid : this.fluidTank.getFluid();
    }

    public Vec3 getSpawnPos(BlockPos pos) {
        return Vec3.atCenterOf(pos).add(getAimVector(this).scale(barrelOffsetDistance()));
    }

    public void fireSpewer(Level level, BlockPos pos, SpewerBlockEntity spewerCannonBlockEntity) {
        if (!this.canShoot()) return;
        spewerCannonBlockEntity.animationTime = spewerCannonBlockEntity.animationLength;
        // Normalize direction
        Vec3 direction = getAimVector(spewerCannonBlockEntity);

        // Offset to spawn in front of the cannon
        Vec3 spawnPos = Vec3.atCenterOf(pos).add(direction.scale(barrelOffsetDistance()));

        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                getShootSound(spewerCannonBlockEntity.getShotFluid()), SoundSource.BLOCKS, 0.25f, 0.75f);

        if (level.isClientSide()) return;
        // Create and launch the projectile
        Projectile projectile;
        FluidStack shot = spewerCannonBlockEntity.getShotFluid();
        boolean fromTank = !spewerCannonBlockEntity.fluidTank.isEmpty();
        Fluid fluid = shot.getFluid();
        if (fluid == Fluids.LAVA) {
            projectile = new Ember(level, spawnPos.x, spawnPos.y, spawnPos.z);
        } else {
            PotionContents potionContents = shot.get(DataComponents.POTION_CONTENTS);
            if (fluid == Fluids.WATER && potionContents != null) {
                projectile = new PotionBlob(level, spawnPos.x, spawnPos.y, spawnPos.z, potionContents);
            } else {
                projectile = new WaterBlob(level, spawnPos.x, spawnPos.y, spawnPos.z);
            }
        }
        projectile.shoot(direction.x, direction.y, direction.z, MUZZLE_VELOCITY, SPREAD);
        level.addFreshEntity(projectile);
        if (fromTank) {
            spewerCannonBlockEntity.fluidTank.drain(10, FluidTank.FluidAction.EXECUTE);
        } else {
            // Draining syncs the spewer to clients; crew liquid doesn't drain, so sync for the recoil animation here
            level.sendBlockUpdated(pos, spewerCannonBlockEntity.getBlockState(), spewerCannonBlockEntity.getBlockState(), 2);
        }
    }

    private static SoundEvent getShootSound(FluidStack shot) {
        return switch (shot.getFluid().isSame(Fluids.LAVA) ? LIQUID_CONTENT.LAVA : LIQUID_CONTENT.WATER) {
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
