package net.luckystudios.blocks.custom.shooting;

import net.luckystudios.entity.custom.seat.Seat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractShootingAimableBlockEntity extends RandomizableContainerBlockEntity {
    protected NonNullList<ItemStack> items;
    public float yaw, pitch;
    public float displayYaw, displayOYaw, displayTYaw;
    public float displayPitch, displayOPitch, displayTPitch;
    public final AnimationState fireAnimation = new AnimationState();
    public float animationTime, animationLength;

    protected AbstractShootingAimableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    }

    public static void extraTick(Level level, BlockPos pos, BlockState state, AbstractShootingAimableBlockEntity shootingAimableBlockEntity) {
        if (level.isClientSide) {
            shootingAimableBlockEntity.displayOYaw = shootingAimableBlockEntity.displayYaw;
            shootingAimableBlockEntity.displayOPitch = shootingAimableBlockEntity.displayPitch;

            // Calculate the shortest angular difference for yaw (in degrees)
            float deltaYaw = shootingAimableBlockEntity.yaw - shootingAimableBlockEntity.displayYaw;
            while (deltaYaw < -180.0F) deltaYaw += 360.0F;
            while (deltaYaw >= 180.0F) deltaYaw -= 360.0F;

            // Smooth it
            shootingAimableBlockEntity.displayYaw += deltaYaw * 0.2F;

            // Same for pitch, which is not circular
            float deltaPitch = shootingAimableBlockEntity.pitch - shootingAimableBlockEntity.displayPitch;
            shootingAimableBlockEntity.displayPitch += deltaPitch * 0.2F;
        }
    }

    // Used to stop the fuse particles from blocking the player's screen.
    // Can be used for other things too
    public static boolean isControllingThisBlock(AbstractShootingAimableBlockEntity shootingAimableBlockEntity) {
        Entity entity = Minecraft.getInstance().player.getVehicle();
        if (entity == null) return false;
        if (!(entity instanceof Seat)) return false;

        BlockPos seatPos = entity.blockPosition();
        BlockPos cannonPos = shootingAimableBlockEntity.getBlockPos();

        // Check if seat is within 1 block of the cannon
        return seatPos.closerThan(cannonPos, 0.5);
    }

    public static Vec3 getRelativeLocationWithOffset(AbstractShootingAimableBlockEntity shootingAimableBlockEntity, Vec3 rotationPointOffset, float upOffset, float forwardOffset, float leftOffset) {
        // Absolute center of rotation (block center + pivot offset)
        Vec3 centerOfRotation = Vec3.atCenterOf(shootingAimableBlockEntity.getBlockPos()).add(rotationPointOffset);

        // Yaw and pitch in radians
        float yawRad = (float) -Math.toRadians(shootingAimableBlockEntity.yaw);
        float pitchRad = (float) -Math.toRadians(shootingAimableBlockEntity.pitch);

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

    public static Vec3 getAimVector(AbstractShootingAimableBlockEntity aimableBlockEntity) {
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

    // Might be useful
    public static BlockPos getBlockPosInFront(AbstractShootingAimableBlockEntity aimableBlockEntity) {
        Vec3 aimVector = AbstractShootingAimableBlockEntity.getAimVector(aimableBlockEntity);
        Vec3i aimVectorInt = new Vec3i((int) aimVector.x, (int) aimVector.y, (int) aimVector.z);
        BlockPos blockPos = aimableBlockEntity.getBlockPos();
        return blockPos.offset(aimVectorInt);
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        this.setChanged();
    }

    public void setPitch(float pitch) {
        // Clamp pitch to -90 to 90 degrees
        this.pitch = Math.clamp(pitch, minPitch(), maxPitch());
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }
        tag.putFloat("yaw", this.yaw);
        tag.putFloat("pitch", this.pitch);
        tag.putFloat("animationTime", this.animationTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // Inventory
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag) && tag.contains("Items", 9)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }

        // Store old display values
        float oldDisplayYaw = this.displayYaw;
        float oldDisplayPitch = this.displayPitch;

        // Aiming Vector
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");

        // Only reset display values if this is the initial load (client-side)
        // Check if display values are uninitialized (0) or if this is server-side
        if (this.level == null || this.level.isClientSide && (oldDisplayYaw == 0.0f && oldDisplayPitch == 0.0f)) {
            this.displayYaw = this.yaw;
            this.displayOYaw = this.yaw;
            this.displayTYaw = this.yaw;
            this.displayPitch = this.pitch;
            this.displayOPitch = this.pitch;
            this.displayTPitch = this.pitch;
        }
        // Otherwise, keep the existing smooth display values

        this.animationTime = tag.getFloat("animationTime");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
        return this.saveWithFullMetadata(lookupProvider);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.items = itemsIn;
    }

    public abstract float maxPitch();

    public abstract float minPitch();

    public abstract float pitchOffset();

    public abstract boolean canShoot();

    public abstract float barrelOffsetDistance();
}
