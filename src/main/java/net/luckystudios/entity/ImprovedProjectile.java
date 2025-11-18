package net.luckystudios.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class ImprovedProjectile extends Projectile {

    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(ImprovedProjectile.class, EntityDataSerializers.BYTE);
    protected Vec3 movementOld;
    protected boolean isStuck = false;
    protected int stuckTime = 0;

    // Needed for registration!
    public ImprovedProjectile(EntityType<? extends ImprovedProjectile> entityType, Level level) {
        super(entityType, level);
        this.movementOld = this.getDeltaMovement();
    }

    // Used when spawning in the projectile with no shooter
    public ImprovedProjectile(EntityType<? extends ImprovedProjectile> entityType, double x, double y, double z, Level level) {
        this(entityType, level);
        this.setPos(x, y, z);
        this.movementOld = this.getDeltaMovement();
    }

    // Used when spawning in the projectile with a shooter
    public ImprovedProjectile(EntityType<? extends ImprovedProjectile> entityType, LivingEntity shooter, Level level) {
        this(entityType, shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ(), level);
        this.setOwner(shooter);
        this.movementOld = this.getDeltaMovement();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID_FLAGS, (byte) 0);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particleoptions = ParticleTypes.FLAME;

            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public double getEyeY() {
        return getBoundingBox().maxY / 2;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d0)) {
            d0 = 4.0;
        }

        d0 *= 64.0;
        return distance < d0 * d0;
    }

    @Override
    public void tick() {
        // Let the parent handle all movement logic
        super.tick();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hitresult)) {
            this.hitTargetOrDeflectSelf(hitresult);
        }

        this.checkInsideBlocks();
        Vec3 vec3 = this.getDeltaMovement();
        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        this.updateRotation();
        float f;
        if (this.isInWater()) {
            for (int i = 0; i < 4; i++) {
                float f1 = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE, d0 - vec3.x * 0.25, d1 - vec3.y * 0.25, d2 - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
            }

            f = 0.8F;
        } else {
            f = 0.99F;
        }

        this.setDeltaMovement(vec3.scale(f));
        this.applyGravity();
        this.setPos(d0, d1, d2);

        this.movementOld = this.getDeltaMovement();

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
        if (getTrailParticles().isEmpty()) return;
        var movement = this.getDeltaMovement();
        double speed = movement.length();

        // Only spawn trail if moving fast enough
        if (speed < 0.1) return;

        // Spawn particles along the path between old position and current position
        Vec3 oldPos = new Vec3(this.xo, this.yo, this.zo);
        Vec3 currentPos = this.position();

        // Calculate how many particles to spawn based on distance traveled
        double distance = oldPos.distanceTo(currentPos);
        int particleCount = Math.max(1, (int)(distance * 8)); // 8 particles per block traveled

        for (ParticleOptions particle : getTrailParticles()) {
            for (int i = 0; i < particleCount; i++) {
                // Interpolate between old and current position
                double t = (double) i / particleCount;
                double x = oldPos.x + (currentPos.x - oldPos.x) * t;
                double y = oldPos.y + (currentPos.y - oldPos.y) * t;
                double z = oldPos.z + (currentPos.z - oldPos.z) * t;

                // Add small random offset to make trail look more natural
                x += (random.nextDouble() - 0.5) * 0.1;
                y += (random.nextDouble() - 0.5) * 0.1;
                z += (random.nextDouble() - 0.5) * 0.1;

                level().addParticle(particle, x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    protected abstract List<ParticleOptions> getTrailParticles();
}
