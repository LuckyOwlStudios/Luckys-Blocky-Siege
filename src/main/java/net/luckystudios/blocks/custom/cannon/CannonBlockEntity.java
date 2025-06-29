package net.luckystudios.blocks.custom.cannon;

import net.luckystudios.blocks.custom.cannon.inventory.CannonBlockMenu;
import net.luckystudios.blocks.util.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class CannonBlockEntity extends BlockEntity implements MenuProvider {
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

    public float yaw, pitch;
    public float displayYaw, displayOYaw, displayTYaw;
    public float displayPitch, displayOPitch, displayTPitch;
    public int firePower;

    public CannonBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        this.setChanged();
    }

    public void setPitch(float pitch) {
        // Clamp pitch to -90 to 90 degrees
        this.pitch = Math.clamp(pitch, -45, 45);
        this.setChanged();
    }

    public void setFirePower(int firePower) {
        this.firePower = firePower;
        this.setChanged();
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getFirePower() {
        return this.firePower;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CannonBlockEntity cannonBlockEntity) {
        cannonBlockEntity.displayOYaw = cannonBlockEntity.displayYaw;
        cannonBlockEntity.displayOPitch = cannonBlockEntity.displayPitch;

        // Calculate the shortest angular difference for yaw (in degrees)
        float deltaYaw = cannonBlockEntity.yaw - cannonBlockEntity.displayYaw;
        while (deltaYaw < -180.0F) deltaYaw += 360.0F;
        while (deltaYaw >= 180.0F) deltaYaw -= 360.0F;

        // Smooth it
        cannonBlockEntity.displayYaw += deltaYaw * 0.2F;

        // Same for pitch, which is not circular
        float deltaPitch = cannonBlockEntity.pitch - cannonBlockEntity.displayPitch;
        cannonBlockEntity.displayPitch += deltaPitch * 0.2F;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putFloat("yaw", this.yaw);
        tag.putFloat("pitch", this.pitch);
        tag.putInt("firePower", this.firePower);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");
        this.firePower = tag.getInt("firePower");

        this.displayYaw = this.yaw;
        this.displayOYaw = this.yaw;
        this.displayTYaw = this.yaw;
        this.displayPitch = this.pitch;
        this.displayOPitch = this.pitch;
        this.displayTPitch = this.pitch;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.blockysiege.cannon");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CannonBlockMenu(containerId, playerInventory, this);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
    }
}
