package net.luckystudios.blocks.custom.cannon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAimableBlockEntity extends BlockEntity {

    public float yaw, pitch;
    public float displayYaw, displayOYaw, displayTYaw;
    public float displayPitch, displayOPitch, displayTPitch;
    
    public AbstractAimableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AbstractAimableBlockEntity aimableBlockEntity) {
        if (level.isClientSide) {
            aimableBlockEntity.displayOYaw = aimableBlockEntity.displayYaw;
            aimableBlockEntity.displayOPitch = aimableBlockEntity.displayPitch;

            // Calculate the shortest angular difference for yaw (in degrees)
            float deltaYaw = aimableBlockEntity.yaw - aimableBlockEntity.displayYaw;
            while (deltaYaw < -180.0F) deltaYaw += 360.0F;
            while (deltaYaw >= 180.0F) deltaYaw -= 360.0F;

            // Smooth it
            aimableBlockEntity.displayYaw += deltaYaw * 0.2F;

            // Same for pitch, which is not circular
            float deltaPitch = aimableBlockEntity.pitch - aimableBlockEntity.displayPitch;
            aimableBlockEntity.displayPitch += deltaPitch * 0.2F;
        }
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

    public static Vec3 getAimVector(AbstractAimableBlockEntity aimableBlockEntity) {
        // Direction vector from yaw and pitch
        float yaw = aimableBlockEntity.yaw;
        float pitch = aimableBlockEntity.pitch;

        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        double xDir = -Math.sin(yawRad) * Math.cos(pitchRad);
        double yDir = Math.sin(pitchRad);
        double zDir = Math.cos(yawRad) * Math.cos(pitchRad);

        // Normalize direction
        return new Vec3(xDir, yDir, zDir).normalize();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("yaw", this.yaw);
        tag.putFloat("pitch", this.pitch);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");

        this.displayYaw = this.yaw;
        this.displayOYaw = this.yaw;
        this.displayTYaw = this.yaw;
        this.displayPitch = this.pitch;
        this.displayOPitch = this.pitch;
        this.displayTPitch = this.pitch;
    }
}
