package net.luckystudios.blocks.custom.cannon;

import net.luckystudios.gui.cannons.ShootingBlockMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractShootingAimableBlockEntity extends AbstractAimableBlockEntity implements MenuProvider {
    public final ItemStackHandler inventory = new ItemStackHandler(inventorySize()) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 64;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    public final AnimationState fireAnimation = new AnimationState();
    public float animationTime, animationLength;
    public int firePower;
    public int cooldown, maxCooldown;

    protected AbstractShootingAimableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.firePower = 1; // Default firepower
        this.cooldown = 0; // Default cooldown
    }

    public abstract int inventorySize();

    public int getFirePower() {
        return this.firePower;
    }

    public static void decreaseCooldown(AbstractShootingAimableBlockEntity shootingAimableBlockEntity) {
        shootingAimableBlockEntity.cooldown--;
        shootingAimableBlockEntity.setChanged();
    }

    public static void extraTick(Level level, BlockPos pos, BlockState state, AbstractShootingAimableBlockEntity shootingAimableBlockEntity) {
    if (level.isClientSide) {
        clientTick(level, pos, state, shootingAimableBlockEntity);
    }
        if (shootingAimableBlockEntity.cooldown > 0) {
            decreaseCooldown(shootingAimableBlockEntity);
        }
        if (shootingAimableBlockEntity.animationTime > 0) {
            shootingAimableBlockEntity.animationTime--;
        }
        shootingAimableBlockEntity.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("firePower", this.firePower);
        tag.putInt("cooldown", this.cooldown);
        tag.putFloat("animationTime", this.animationTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        this.firePower = tag.getInt("firePower");
        this.cooldown = tag.getInt("cooldown");
        this.animationTime = tag.getFloat("animationTime");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ShootingBlockMenu(containerId, playerInventory, this);
    }

    public static Vec3 getRelativeLocationWithOffset(AbstractAimableBlockEntity aimableBlockEntity, Vec3 rotationPointOffset, float upOffset, float forwardOffset, float leftOffset) {
        // Absolute center of rotation (block center + pivot offset)
        Vec3 centerOfRotation = Vec3.atCenterOf(aimableBlockEntity.getBlockPos()).add(rotationPointOffset);

        // Yaw and pitch in radians
        float yawRad = (float) -Math.toRadians(aimableBlockEntity.yaw);
        float pitchRad = (float) -Math.toRadians(aimableBlockEntity.pitch);

        // Forward vector from yaw and pitch
        Vec3 forward = new Vec3(
                -Math.sin(yawRad) * Math.cos(pitchRad),
                Math.sin(pitchRad),
                -Math.cos(yawRad) * Math.cos(pitchRad)
        ).normalize();

        // Compute right and up vectors
        Vec3 globalUp = new Vec3(0, 1, 0);
        Vec3 right = globalUp.cross(forward).normalize();  // Right relative to forward
        Vec3 up = forward.cross(right).normalize();        // Local up

        // Final offset
        Vec3 offset = forward.scale(forwardOffset)
                .add(up.scale(upOffset))
                .subtract(right.scale(leftOffset)); // Left = -right

        return centerOfRotation.add(offset);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    public boolean canShoot(AbstractShootingAimableBlockEntity aimableBlockEntity, boolean checkCooldown) {
        ItemStack fuseStack = aimableBlockEntity.inventory.getStackInSlot(0);
        ItemStack ammoStack = aimableBlockEntity.inventory.getStackInSlot(1);
        boolean cooldownCheck = !checkCooldown || aimableBlockEntity.cooldown <= 0;
        return hasFuse(fuseStack) && hasAmmo(ammoStack) && cooldownCheck;
    }

    // Abstract method to be implemented by subclasses
    public abstract boolean hasFuse(ItemStack fuseStack);

    // Abstract method to be implemented by subclasses
    public abstract boolean hasAmmo(ItemStack ammoStack);
}
