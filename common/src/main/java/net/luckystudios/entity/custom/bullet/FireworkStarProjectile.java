package net.luckystudios.entity.custom.bullet;

import net.minecraft.core.registries.Registries;
import it.unimi.dsi.fastutil.ints.IntList;
import net.luckystudios.entity.custom.ImprovedProjectileEntity;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.init.ModDamageTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FireworkStarProjectile extends ImprovedProjectileEntity {
    private static final EntityDataAccessor<ItemStack> FIREWORK_STAR_ITEM = SynchedEntityData.defineId(FireworkStarProjectile.class, EntityDataSerializers.ITEM_STACK);
    protected Vec3 movementOld;
    /** Entities this bullet has already punched through (star shape pierces). */
    private final Set<Integer> piercedEntities = new HashSet<>();
    private boolean detonated = false;

    /** How many extra entities a STAR shaped bullet can punch through. */
    private static final int STAR_EXTRA_PIERCES = 2;
    private static final float BASE_DAMAGE = 6.0F;
    private static final double HOMING_RANGE = 10.0;
    private static final double EFFECT_RADIUS = 3.0;

    public FireworkStarProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FireworkStarProjectile(Level level, double x, double y, double z, ItemStack fireworkStarStack) {
        super(ModEntityTypes.FIREWORK_STAR.get(), x, y, z, level);
        this.entityData.set(FIREWORK_STAR_ITEM, fireworkStarStack.copy());
        this.movementOld = this.getDeltaMovement();
    }

    public FireworkStarProjectile(LivingEntity shooter, Level level) {
        super(ModEntityTypes.FIREWORK_STAR.get(), shooter, level);
        this.movementOld = this.getDeltaMovement();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FIREWORK_STAR_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIREWORK_STAR;
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 vec3 = this.getMovementToShoot(x, y, z, velocity, inaccuracy);
        this.setDeltaMovement(vec3);
        this.hasImpulse = true;
        double d0 = vec3.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)180.0F / (double)(float)Math.PI));
        this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)180.0F / (double)(float)Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public void spawnTrailParticles() {
        if (this.isStuck) return;
        ItemStack fireChargeStack = this.getEntityData().get(FIREWORK_STAR_ITEM);
        FireworkExplosion fireworkExplosion = fireChargeStack.get(DataComponents.FIREWORK_EXPLOSION);
        handleTrail(fireworkExplosion);
    }

    private void handleTrail(FireworkExplosion fireworkExplosion) {
        Vec3 newPos = this.position();
        if (fireworkExplosion == null) {
            fireworkExplosion = getExplosion();
        }

        int color = StarBurst.pickColor(fireworkExplosion == null ? IntList.of() : fireworkExplosion.colors(), random);
        if (fireworkExplosion == null || fireworkExplosion.colors().isEmpty()) {
            color = 8421504; // Gray when no firework explosion data
        }
        // Fade colours: the trail dust shifts from the star's colour to its fade colour
        int fade = color;
        if (fireworkExplosion != null && !fireworkExplosion.fadeColors().isEmpty()) {
            fade = StarBurst.pickColor(fireworkExplosion.fadeColors(), random);
        }

        Vector3f from = StarBurst.toVector(color);
        Vector3f to = StarBurst.toVector(fade);

        if (this.tickCount > 1) {
            double dx = newPos.x - xo;
            double dy = newPos.y - yo;
            double dz = newPos.z - zo;
            int s = 4;
            for (int i = 0; i < s; ++i) {
                double j = i / (double) s;
                this.level().addParticle(new DustColorTransitionOptions(from, to, 1.0F),
                        xo - dx * j,
                        0.25 + yo - dy * j,
                        zo - dz * j,
                        0, 0.02, 0);
            }
            if (fireworkExplosion != null) {
                // Trail (diamond): a bright end rod tracer. Twinkle (glowstone): crackling sparks.
                if (fireworkExplosion.hasTrail()) {
                    this.level().addParticle(ParticleTypes.END_ROD, xo, 0.25 + yo, zo, 0, 0, 0);
                }
                if (fireworkExplosion.hasTwinkle() && random.nextInt(2) == 0) {
                    this.level().addParticle(ParticleTypes.FIREWORK, xo, 0.25 + yo, zo,
                            random.nextGaussian() * 0.03, random.nextGaussian() * 0.03, random.nextGaussian() * 0.03);
                }
            }
        }
    }

    private FireworkExplosion getExplosion() {
        FireworkExplosion explosion = this.getEntityData().get(FIREWORK_STAR_ITEM).get(DataComponents.FIREWORK_EXPLOSION);
        if (explosion == null) {
            explosion = this.getItem().get(DataComponents.FIREWORK_EXPLOSION);
        }
        return explosion;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && !this.isStuck && !this.isRemoved()) {
            FireworkExplosion explosion = getExplosion();
            // Trail (diamond): the bullet gently homes in on living targets ahead of it
            if (explosion != null && explosion.hasTrail()) {
                steerTowardsTarget();
            }
            if (this.tickCount > 200) {
                this.detonate(this.position(), null);
                this.discard();
            }
        }
    }

    private void steerTowardsTarget() {
        Vec3 motion = this.getDeltaMovement();
        double speed = motion.length();
        if (speed < 0.01) return;
        Vec3 heading = motion.normalize();
        Entity owner = this.getOwner();
        AABB box = this.getBoundingBox().inflate(HOMING_RANGE);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && e != owner && !e.isSpectator() && !piercedEntities.contains(e.getId()))) {
            Vec3 to = candidate.getBoundingBox().getCenter().subtract(this.position());
            double dist = to.length();
            if (dist < 0.001 || dist > HOMING_RANGE || to.normalize().dot(heading) < 0.6) continue;
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        if (best == null) return;
        Vec3 desired = best.getBoundingBox().getCenter().subtract(this.position()).normalize();
        Vec3 steered = heading.scale(0.8).add(desired.scale(0.2)).normalize().scale(speed);
        this.setDeltaMovement(steered);
        this.hasImpulse = true;
    }

    private int distinctColorCount(FireworkExplosion explosion) {
        return explosion == null ? 0 : (int) explosion.colors().intStream().distinct().count();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return !piercedEntities.contains(target.getId()) && super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;
        Entity entity = result.getEntity();
        FireworkExplosion explosion = getExplosion();
        // Colourful (3+ distinct colours): +1 damage per extra colour, up to +3
        float damage = BASE_DAMAGE + Math.min(3, Math.max(0, distinctColorCount(explosion) - 1));
        entity.hurt(new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.BULLET), this, this.getOwner()), damage);
        if (entity instanceof LivingEntity living) {
            applyStatusEffects(living, explosion);
        }
        // Star shape: punch through a couple of targets before bursting
        if (explosion != null && explosion.shape() == FireworkExplosion.Shape.STAR
                && piercedEntities.size() < STAR_EXTRA_PIERCES) {
            piercedEntities.add(entity.getId());
            if (this.level() instanceof ServerLevel serverLevel) {
                StarBurst.spawn(serverLevel, result.getLocation(), this.getDeltaMovement(),
                        new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, explosion.colors(), explosion.fadeColors(), false, false));
            }
            return;
        }
        this.detonate(result.getLocation(), entity);
        this.discard();
    }

    /** Status effects a star gives to one target: twinkle makes them glow, fade colours slow them. */
    private void applyStatusEffects(LivingEntity living, FireworkExplosion explosion) {
        if (explosion == null) return;
        if (explosion.hasTwinkle()) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        }
        if (!explosion.fadeColors().isEmpty()) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }
    }

    /** Impact burst. Every property of the firework star adds something here, see the PR for the full table. */
    private void detonate(Vec3 pos, Entity directHit) {
        if (detonated || !(this.level() instanceof ServerLevel serverLevel)) return;
        detonated = true;
        FireworkExplosion explosion = getExplosion();
        if (explosion == null) return;

        Vec3 facing = this.getDeltaMovement();
        StarBurst.spawn(serverLevel, pos, facing, explosion);

        boolean large = explosion.shape() == FireworkExplosion.Shape.LARGE_BALL;
        serverLevel.playSound(null, pos.x, pos.y, pos.z,
                large ? SoundEvents.FIREWORK_ROCKET_LARGE_BLAST : SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.NEUTRAL, 2.0F, 1.0F);
        if (explosion.hasTwinkle()) {
            serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.NEUTRAL, 2.0F, 1.0F);
        }

        Entity owner = this.getOwner();
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos, pos).inflate(EFFECT_RADIUS),
                e -> e != directHit && e != owner && e.isAlive() && e.position().distanceTo(pos) <= EFFECT_RADIUS);
        DamageSource source = new DamageSource(serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.BULLET), this, owner);

        switch (explosion.shape()) {
            case LARGE_BALL -> // Fire charge: a tiny, block-safe explosion
                    serverLevel.explode(this, pos.x, pos.y, pos.z, 1.2F, Level.ExplosionInteraction.NONE);
            case CREEPER -> {
                // Creeper head: shrapnel and a violent shove for everything close by, plus a hiss
                serverLevel.playSound(null, pos.x, pos.y, pos.z, SoundEvents.CREEPER_PRIMED, SoundSource.NEUTRAL, 1.0F, 1.2F);
                for (LivingEntity e : nearby) {
                    e.hurt(source, 3.0F);
                    Vec3 push = e.position().subtract(pos).multiply(1, 0, 1).normalize().scale(0.9);
                    e.push(push.x, 0.4, push.z);
                    e.hurtMarked = true;
                }
            }
            case BURST -> {
                // Feather: a puff that lifts nearby mobs off the ground
                for (LivingEntity e : nearby) {
                    e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1));
                }
                serverLevel.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 12, 0.4, 0.2, 0.4, 0.05);
            }
            default -> {
            }
        }
        for (LivingEntity e : nearby) {
            applyStatusEffects(e, explosion);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        BlockState blockState = this.level().getBlockState(result.getBlockPos());

        if (!this.level().isClientSide()) {  // Server side for block breaking
            float destroySpeed = blockState.getDestroySpeed(this.level(), result.getBlockPos());

            // Break weak blocks (glass = 0.3, leaves = 0.2, etc.)
            if (destroySpeed >= 0 && destroySpeed <= 0.4f) {
                this.level().destroyBlock(result.getBlockPos(), true); // true = drop items
                // Don't discard - let projectile continue through
                return;
            }
            // Anything tougher stops the bullet and bursts
            this.detonate(result.getLocation(), null);
            this.discard();
        } else {  // Client side for particles
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                        this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }
}
