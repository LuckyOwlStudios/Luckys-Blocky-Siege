package net.luckystudios.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class ImprovedProjectileEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(ImprovedProjectileEntity.class, EntityDataSerializers.BYTE);
    protected Vec3 movementOld;
    protected boolean isStuck = false;
    protected int stuckTime = 0;

    public ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        this.movementOld = this.getDeltaMovement();
    }

    public ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }

    public ImprovedProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_FLAGS, (byte) 0);
    }

    @Override
    public double getEyeY() {
        return getBoundingBox().maxY / 2;
    }

    @Override
    public void tick() {
        // Let the parent handle all movement logic
        super.tick();

        // Check if stuck (entity isn't moving much)
        this.isStuck = this.position().subtract(this.xo, this.yo, this.zo).lengthSqr() < (0.0001 * 0.0001);

        // Track how long we've been stuck
        if (this.isStuck) {
            this.stuckTime++;
        } else {
            this.stuckTime = 0;
        }

        // Spawn trail particles only when not stuck and on client side
        if (!isStuck && this.level().isClientSide) {
            this.spawnTrailParticles();
        }
    }

    public void spawnTrailParticles() {
        if (this.isInWater()) {
            // Projectile particle code
            var movement = this.getDeltaMovement();
            double velX = movement.x;
            double velY = movement.y;
            double velZ = movement.z;
            for (int j = 0; j < 4; ++j) {
                double pY = this.getEyeY();
                level().addParticle(ParticleTypes.BUBBLE,
                        getX() - velX * 0.25D, pY - velY * 0.25D, getZ() - velZ * 0.25D,
                        velX, velY, velZ);
            }
        }
    }
}
