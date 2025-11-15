package net.luckystudios.entity.custom.seat;

import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class Seat extends Entity {

    public Seat(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public Seat(Level level, BlockPos pos) {
        super(ModEntityTypes.SEAT.get(), level);
        float xx = pos.getX() + 0.5f;
        float yy = pos.getY();
        float zz = pos.getZ() + 0.5f;
        this.setPos(xx, yy, zz);
        this.xo = xx;
        this.yo = yy;
        this.zo = zz;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (this.getControllingPassenger() == null) return;
        if (!(this.getControllingPassenger() instanceof LivingEntity livingEntity)) return;
        this.setRot(livingEntity.getYRot(), livingEntity.getXRot() * 0.5F);
        if (!(level.getBlockEntity(blockPosition()) instanceof AbstractShootingAimableBlockEntity aimableBlockEntity)) {
            this.discard();
            return;
        }
        float playerYaw = livingEntity.getYRot();
        float playerPitch = livingEntity.getXRot();
        float normalizedYaw = (playerYaw % 360 + 360) % 360;
        float flippedPitch = -playerPitch;
        aimableBlockEntity.setYaw(normalizedYaw);
        aimableBlockEntity.setPitch(flippedPitch + aimableBlockEntity.pitchOffset());
        aimableBlockEntity.setChanged();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        // How far behind the entity you want the passenger to sit
        double backOffset = 1.0;

        // Convert yaw to radians (yaw is in degrees)
        double yawRad = Math.toRadians(this.getYRot());

        // Calculate the offset vector (behind the entity)
        double offsetX = -Math.sin(yawRad) * -backOffset;
        double offsetZ = Math.cos(yawRad) * -backOffset;

        // Return the new attachment point (offset relative to entity's center)
        return new Vec3(offsetX, 0.5, offsetZ);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    // we don't need to read or write any data to the NBT.
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {}
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {}

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity livingEntity
                ? livingEntity
                : super.getControllingPassenger();
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.discard();
    }
}
