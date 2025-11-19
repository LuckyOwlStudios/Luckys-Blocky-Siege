package net.luckystudios.entity.custom.cannonball;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractCannonBall extends AbstractNewProjectile {

    protected Vec3 movementOld;
    protected boolean isStuck = false;
    protected int stuckTime = 0;

    protected AbstractCannonBall(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
        this.movementOld = this.getDeltaMovement();
    }

    protected AbstractCannonBall(EntityType<? extends AbstractNewProjectile> entityType, double x, double y, double z, Level level, ItemStack pickupItemStack) {
        super(entityType, x, y, z, level, pickupItemStack);
        this.setBaseDamage(baseDamage());
        this.pickup = Pickup.ALLOWED;
    }

    protected AbstractCannonBall(EntityType<? extends AbstractNewProjectile> entityType, LivingEntity owner, Level level, ItemStack pickupItemStack) {
        super(entityType, owner, level, pickupItemStack);
        this.setBaseDamage(baseDamage());
        this.pickup = Pickup.ALLOWED;
    }

    protected abstract float baseDamage();

    protected abstract SoundEvent impactSound();


    @Override
    public void tick() {
        super.tick();

        Level level = this.level();
        Vec3 movement = this.getDeltaMovement();
        this.movementOld = movement;

        // This causes the projectile to bounce off walls and such, can use again in the future under more polishing
//        this.move(MoverType.SELF, movement);

        // rest stuff
        this.tryCheckInsideBlocks();

        if (!isStuck) {
            if (level.isClientSide) {
                this.spawnTrailParticles();
            }

            this.updateRotation();
        }

        // check if stuck
        this.isStuck = !this.noPhysics && this.position().subtract(this.xo, this.yo, this.zo).lengthSqr() < (0.0001 * 0.0001);
        if (this.isStuck) {
            this.setGlowingTag(false);
            level.broadcastEntityEvent(this, (byte)0);
            this.setPickupItemStack(new ItemStack(getPickupItem().getItem()));
        }
    }

    protected float getInertia() {
        // normally 0.99 for everything
        return 0.99F;
    }

    protected float getWaterInertia() {
        // normally 0.6 for arrows and 0.99 for tridents and 0.8 for other projectiles
        return 0.6F;
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

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), impactSound(), this.getSoundSource(), 1.0F, 1.0F);
        }
    }
}
