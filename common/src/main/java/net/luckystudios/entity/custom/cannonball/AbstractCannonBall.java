package net.luckystudios.entity.custom.cannonball;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractCannonBall extends AbstractNewProjectile {

    /** Number of player hits needed to knock a landed cannonball loose. */
    public static final int HITS_TO_DISLODGE = 5;
    private static final int WOBBLE_DURATION = 10;
    private static final int HIT_RESET_TICKS = 60;

    // Synced so the client knows when the ball has come to rest, and how hard it has been hit.
    private static final EntityDataAccessor<Boolean> DATA_STUCK = SynchedEntityData.defineId(AbstractCannonBall.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_HIT_COUNT = SynchedEntityData.defineId(AbstractCannonBall.class, EntityDataSerializers.INT);

    protected Vec3 movementOld;
    protected boolean isStuck = false;
    protected int stuckTime = 0;
    /** True once the ball has detonated on its first impact, so resting/rolling balls don't explode again. */
    protected boolean exploded = false;

    private int wobbleTicks = 0;
    private int wobbleDirection = 1;
    private int lastHitTick = 0;

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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STUCK, false);
        builder.define(DATA_HIT_COUNT, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (this.level().isClientSide && DATA_HIT_COUNT.equals(key) && this.entityData.get(DATA_HIT_COUNT) > 0) {
            this.wobbleTicks = WOBBLE_DURATION;
            this.wobbleDirection = -this.wobbleDirection;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("exploded", this.exploded);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.exploded = compound.getBoolean("exploded");
    }

    protected abstract float baseDamage();

    protected abstract SoundEvent impactSound();


    @Override
    public void tick() {
        // The client mirrors the server's "at rest" state instead of predicting its own landing.
        if (this.level().isClientSide) {
            this.inGround = this.entityData.get(DATA_STUCK);
        }
        super.tick();
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_STUCK, this.inGround && !this.isNoPhysics());
            if (this.getHitCount() > 0 && this.tickCount - this.lastHitTick > HIT_RESET_TICKS) {
                this.entityData.set(DATA_HIT_COUNT, 0);
            }
        }
        if (this.wobbleTicks > 0) {
            this.wobbleTicks--;
        }

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
        this.isStuck = this.inGround && !this.isNoPhysics();
        if (this.isStuck) {
            this.setGlowingTag(false);
            level.broadcastEntityEvent(this, (byte)0);
            this.setPickupItemStack(new ItemStack(getPickupItem().getItem()));
        }
    }

    // --- Landed behaviour: sticks in the ground like a trident, but must be hit to pick it up ---

    /** Landed cannonballs never despawn on their own. */
    @Override
    protected void tickDespawn() {
    }

    /** Walking over a landed cannonball does nothing. */
    @Override
    public void playerTouch(Player player) {
    }

    @Override
    public boolean isPickable() {
        return this.inGround ? !this.isRemoved() : super.isPickable();
    }

    /** Projectiles pass through a landed ball rather than being eaten by it. */
    @Override
    public boolean canBeHitByProjectile() {
        return !this.inGround && super.canBeHitByProjectile();
    }

    @Override
    public boolean isAttackable() {
        return this.inGround || super.isAttackable();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.inGround || this.isRemoved() || this.isInvulnerableTo(source)) return false;
        if (!(source.getDirectEntity() instanceof Player)) return false;
        if (this.level().isClientSide) return true;

        int hits = this.getHitCount() + 1;
        this.lastHitTick = this.tickCount;
        if (hits >= HITS_TO_DISLODGE) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.NETHERITE_BLOCK_BREAK, this.getSoundSource(), 1.0F, 0.8F);
            this.spawnAtLocation(this.getPickupItem(), 0.1F);
            this.discard();
        } else {
            this.entityData.set(DATA_HIT_COUNT, hits);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.NETHERITE_BLOCK_HIT, this.getSoundSource(), 1.0F, 0.8F + 0.1F * hits);
        }
        return true;
    }

    public int getHitCount() {
        return this.entityData.get(DATA_HIT_COUNT);
    }

    /** Remaining wobble time in ticks (client-side animation only). */
    public int getWobbleTicks() {
        return this.wobbleTicks;
    }

    public int getWobbleDirection() {
        return this.wobbleDirection;
    }

    public static int getWobbleDuration() {
        return WOBBLE_DURATION;
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
