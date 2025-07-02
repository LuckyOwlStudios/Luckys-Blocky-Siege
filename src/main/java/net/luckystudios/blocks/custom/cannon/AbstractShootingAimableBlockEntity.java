package net.luckystudios.blocks.custom.cannon;

import net.luckystudios.blocks.custom.cannon.inventory.CannonBlockMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractShootingAimableBlockEntity extends AbstractAimableBlockEntity implements MenuProvider {

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
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
    public int firePower;
    public int cooldown;

    protected AbstractShootingAimableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.firePower = 1; // Default fire power
    }

    public int getFirePower() {
        return this.firePower;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("firePower", this.firePower);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        this.firePower = tag.getInt("firePower");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CannonBlockMenu(containerId, playerInventory, this);
    }

    public static Vec3 getParticleLocation(AbstractAimableBlockEntity aimableBlockEntity, Vec3 rotationPointOffset, float upOffset, float forwardOffset) {
        // Absolute center of rotation (block center + pivot offset)
        Vec3 centerOfRotation = Vec3.atCenterOf(aimableBlockEntity.getBlockPos()).add(rotationPointOffset);

        // Yaw and pitch in radians
        float yawRad = (float) -Math.toRadians(aimableBlockEntity.yaw);
        float pitchRad = (float) -Math.toRadians(aimableBlockEntity.pitch);

        // Forward vector from yaw and pitch
        Vec3 forward = new Vec3(
                -Math.sin(yawRad) * Math.cos(pitchRad),
                Math.sin(pitchRad),
                -Math.cos(yawRad) * Math.cos(pitchRad)  // negative Z to match MC forward
        ).normalize();

        // Compute right and local up vectors
        Vec3 globalUp = new Vec3(0, 1, 0);
        Vec3 right = globalUp.cross(forward).normalize();
        Vec3 up = forward.cross(right).normalize();

        // Offset from rotation point in local space
        Vec3 offset = forward.scale(forwardOffset).add(up.scale(upOffset));

        // Final world position
        return centerOfRotation.add(offset);
    }

    private boolean isFarAwayFromCamera() {
        if (this.level == null || !this.level.isClientSide) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        double dx = mc.player.getX() - (this.worldPosition.getX() + 0.5);
        double dy = mc.player.getY() - (this.worldPosition.getY() + 0.5);
        double dz = mc.player.getZ() - (this.worldPosition.getZ() + 0.5);
        double distSq = dx * dx + dy * dy + dz * dz;
        return distSq >= 256.0; // 16 blocks or more
    }

}
