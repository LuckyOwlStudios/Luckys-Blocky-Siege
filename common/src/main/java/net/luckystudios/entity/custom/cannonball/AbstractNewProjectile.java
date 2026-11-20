package net.luckystudios.entity.custom.cannonball;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.luckystudios.init.ModDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import org.jetbrains.annotations.Nullable;
import java.util.List;

// This is basically a copy of the AbstractArrow class, but with modifications to suit the needs of a cannonball.
public abstract class AbstractNewProjectile extends Projectile {

    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractNewProjectile.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractNewProjectile.class, EntityDataSerializers.BYTE);

    private static final int FLAG_NO_PHYSICS = 2;
    private @Nullable BlockState lastState;
    protected boolean inGround;
    protected int inGroundTime;
    public Pickup pickup = Pickup.DISALLOWED;
    private int life;
    private double baseDamage = 2.0;
    private @Nullable IntOpenHashSet piercingIgnoreEntityIds;
    private @Nullable List<Entity> piercedAndKilledEntities;
    private ItemStack pickupItemStack = this.getDefaultPickupItem();

    protected AbstractNewProjectile(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
    }

    protected AbstractNewProjectile(
            EntityType<? extends AbstractNewProjectile> entityType,
            double x,
            double y,
            double z,
            Level level,
            ItemStack pickupItemStack
    ) {
        this(entityType, level);
        this.pickupItemStack = pickupItemStack.copy();
        this.setCustomName(pickupItemStack.get(DataComponents.CUSTOM_NAME));
        Unit unit = pickupItemStack.remove(DataComponents.INTANGIBLE_PROJECTILE);
        if (unit != null) {
            this.pickup = Pickup.CREATIVE_ONLY;
        }
        this.setPierceLevel((byte) 8);
        this.setPos(x, y, z);
    }

    protected AbstractNewProjectile(
            EntityType<? extends AbstractNewProjectile> entityType, LivingEntity owner, Level level, ItemStack pickupItemStack
    ) {
        this(entityType, owner.getX(), owner.getEyeY() - 0.1F, owner.getZ(), level, pickupItemStack);
        this.setOwner(owner);
    }

    /**
     * Checks if the entity is in range to render.
     */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(d0)) {
            d0 = 1.0;
        }

        d0 *= 64.0 * getViewScale();
        return distance < d0 * d0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID_FLAGS, (byte)0);
        builder.define(PIERCE_LEVEL, (byte)0);
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to an x, y, z direction.
     */
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        this.life = 0;
    }

    /**
     * Updates the entity motion clientside, called by packets from the server
     */
    @Override
    public void lerpMotion(double x, double y, double z) {
        super.lerpMotion(x, y, z);
        this.life = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.usesBallPhysics() && !this.isNoPhysics()) {
            this.tickBallPhysics();
            return;
        }
        boolean flag = this.isNoPhysics();
        Vec3 vec3 = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = vec3.horizontalDistance();
            this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
            this.setXRot((float)(Mth.atan2(vec3.y, d0) * 180.0F / (float)Math.PI));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level().getBlockState(blockpos);
        if (!blockstate.isAir() && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
            if (!voxelshape.isEmpty()) {
                Vec3 vec31 = this.position();

                for (AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(vec31)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.inGround && !flag) {
            if (this.lastState != blockstate && this.shouldFall()) {
                this.startFalling();
            } else if (!this.level().isClientSide) {
                this.tickDespawn();
            }

            this.inGroundTime++;
        } else {
            this.inGroundTime = 0;
            Vec3 vec32 = this.position();
            Vec3 vec33 = vec32.add(vec3);
            HitResult hitresult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hitresult.getType() != HitResult.Type.MISS) {
                vec33 = hitresult.getLocation();
            }

            while (!this.isRemoved()) {
                EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult)hitresult).getEntity();
                    Entity entity1 = this.getOwner();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !flag) {
                    if (net.luckystudios.platform.Services.PLATFORM.onProjectileImpact(this, hitresult))
                        break;
                    ProjectileDeflection projectiledeflection = this.hitTargetOrDeflectSelf(hitresult);
                    this.hasImpulse = true;
                    if (projectiledeflection != ProjectileDeflection.NONE) {
                        break;
                    }
                }

                if (entityhitresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                hitresult = null;
            }

            vec3 = this.getDeltaMovement();
            double d5 = vec3.x;
            double d6 = vec3.y;
            double d1 = vec3.z;

            double d7 = this.getX() + d5;
            double d2 = this.getY() + d6;
            double d3 = this.getZ() + d1;
            double d4 = vec3.horizontalDistance();
            if (flag) {
                this.setYRot((float)(Mth.atan2(-d5, -d1) * 180.0F / (float)Math.PI));
            } else {
                this.setYRot((float)(Mth.atan2(d5, d1) * 180.0F / (float)Math.PI));
            }

            this.setXRot((float)(Mth.atan2(d6, d4) * 180.0F / (float)Math.PI));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f = 0.99F;
            if (this.isInWater()) {
                for (int j = 0; j < 4; j++) {
                    this.level().addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25, d2 - d6 * 0.25, d3 - d1 * 0.25, d5, d6, d1);
                }

                f = this.getWaterInertia();
            }

            this.setDeltaMovement(vec3.scale(f));
            if (!flag) {
                this.applyGravity();
            }

            this.setPos(d7, d2, d3);
            this.checkInsideBlocks();
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    private boolean shouldFall() {
        return this.inGround && this.level().noCollision(new AABB(this.position(), this.position()).inflate(0.06));
    }

    private void startFalling() {
        this.inGround = false;
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(
                vec3.multiply(this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F)
        );
        this.life = 0;
    }

    @Override
    public void move(MoverType type, Vec3 pos) {
        super.move(type, pos);
        if (type != MoverType.SELF && this.shouldFall()) {
            this.startFalling();
        }
    }

    protected void tickDespawn() {
        this.life++;
        if (this.life >= 1200) {
            this.discard();
        }
    }

    private void resetPiercedEntities() {
        if (this.piercedAndKilledEntities != null) {
            this.piercedAndKilledEntities.clear();
        }

        if (this.piercingIgnoreEntityIds != null) {
            this.piercingIgnoreEntityIds.clear();
        }
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.usesBallPhysics()) {
            this.ballHitEntity(result);
            return;
        }
        Entity entity = result.getEntity();
        float f = (float)this.getDeltaMovement().length();
        double d0 = this.baseDamage;
        Entity entity1 = this.getOwner();
        DamageSource damagesource = this.damageSources().source(ModDamageTypes.CANNON, entity1 != null ? entity1 : this);

        int j = Mth.ceil(Mth.clamp((double)f * d0, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (entity1 instanceof LivingEntity livingEntity) {
            livingEntity.setLastHurtMob(entity);
        }

        boolean flag = entity.getType() == EntityType.ENDERMAN;
        int i = entity.getRemainingFireTicks();
        if (this.isOnFire() && !flag) {
            entity.igniteForSeconds(5.0F);
        }

        if (entity.hurt(damagesource, (float)j)) {
            if (flag) {
                return;
            }

            if (entity instanceof LivingEntity livingentity) {
                if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                this.doKnockback(livingentity, damagesource);
                if (this.level() instanceof ServerLevel serverLevel) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, livingentity, damagesource, this.getWeaponItem());
                }

                if (livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer)entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }
            }
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }

            // 💥 Bounce logic: only bounce if the target is still alive (boats, minecarts and other
            // non-living targets used to crash the game here)
            if (entity.isAlive()) {
                bounceBack(result);
            } else if (this.getPierceLevel() <= 0) {
                this.discard();
            }

        } else {
            entity.setRemainingFireTicks(i);
            this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            }
        }
    }

    private void bounceBack(EntityHitResult result) {
        Vec3 motion = this.getDeltaMovement();
        Vec3 normal = result.getLocation().subtract(this.position()).normalize();

        // Reflect motion vector around normal
        Vec3 reflected = motion.subtract(normal.scale(2 * motion.dot(normal))).scale(0.5); // lose 50% velocity
        this.setDeltaMovement(reflected);

        // Slight vertical lift to avoid sticking
        if (reflected.y == 0) {
            this.setDeltaMovement(reflected.x, 0.1, reflected.z);
        }

        // Rotate the projectile to face its new direction
        this.setYRot((float) (Math.toDegrees(Math.atan2(reflected.z, reflected.x))) - 90.0F);
        this.setXRot((float) (Math.toDegrees(-Math.atan2(reflected.y, Math.sqrt(reflected.x * reflected.x + reflected.z * reflected.z)))));

        // Optionally: play a sound or particle effect here
    }


    protected void doKnockback(LivingEntity entity, DamageSource damageSource) {
        double d1 = Math.max(0.0, 1.0 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(0.25 * 0.6 * d1);
        if (vec3.lengthSqr() > 0.0) {
            entity.push(vec3.x, 0.1, vec3.z);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.lastState = this.level().getBlockState(result.getBlockPos());
        super.onHitBlock(result);
        if (this.usesBallPhysics()) {
            // Balls don't stick: they bounce, skim or roll off whatever they hit
            this.resetPiercedEntities();
            this.bounceOffBlock(result);
            return;
        }
        Vec3 vec3 = result.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.inGround = true;
        this.setPierceLevel((byte)0);
        this.resetPiercedEntities();
    }

    /**
     * Gets the EntityRayTraceResult representing the entity hit
     */
    @Nullable
    protected EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        return ProjectileUtil.getEntityHitResult(
                this.level(), this, startVec, endVec, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity
        );
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(target.getId()));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putShort("life", (short)this.life);
        if (this.lastState != null) {
            compound.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
        }

        compound.putBoolean("inGround", this.inGround);
        compound.putByte("pickup", (byte)this.pickup.ordinal());
        compound.putDouble("damage", this.baseDamage);
        compound.putByte("PierceLevel", this.getPierceLevel());
        compound.put("item", this.pickupItemStack.save(this.registryAccess()));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.life = compound.getShort("life");
        if (compound.contains("inBlockState", 10)) {
            this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compound.getCompound("inBlockState"));
        }

        this.inGround = compound.getBoolean("inGround");
        if (compound.contains("damage", 99)) {
            this.baseDamage = compound.getDouble("damage");
        }

        this.pickup = Pickup.byOrdinal(compound.getByte("pickup"));
        this.setPierceLevel(compound.getByte("PierceLevel"));

        if (compound.contains("item", 10)) {
            this.setPickupItemStack(ItemStack.parse(this.registryAccess(), compound.getCompound("item")).orElse(this.getDefaultPickupItem()));
        } else {
            this.setPickupItemStack(this.getDefaultPickupItem());
        }
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);

        this.pickup = switch (entity) {
            case Player player when this.pickup == Pickup.DISALLOWED -> Pickup.ALLOWED;
            case OminousItemSpawner ominousitemspawner -> Pickup.DISALLOWED;
            case null, default -> this.pickup;
        };
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    @Override
    public void playerTouch(Player entity) {
        if (!this.level().isClientSide && (this.inGround || this.isNoPhysics())) {
            if (this.tryPickup(entity)) {
                entity.take(this, 1);
                this.discard();
            }
        }
    }

    protected boolean tryPickup(Player player) {
        return switch (this.pickup) {
            case DISALLOWED -> false;
            case ALLOWED -> player.getInventory().add(this.getPickupItem());
            case CREATIVE_ONLY -> player.hasInfiniteMaterials();
        };
    }

    protected ItemStack getPickupItem() {
        return this.pickupItemStack.copy();
    }

    protected abstract ItemStack getDefaultPickupItem();

    @Override
    protected Entity.@NotNull MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    public ItemStack getPickupItemStackOrigin() {
        return this.pickupItemStack;
    }

    public void setBaseDamage(double baseDamage) {
        this.baseDamage = baseDamage;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }

    @Override
    public boolean isAttackable() {
        return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
    }

    public byte getPierceLevel() {
        return this.entityData.get(PIERCE_LEVEL);
    }

    private void setPierceLevel(byte pierceLevel) {
        this.entityData.set(PIERCE_LEVEL, pierceLevel);
    }

    private void setFlag(int id, boolean value) {
        byte b0 = this.entityData.get(ID_FLAGS);
        if (value) {
            this.entityData.set(ID_FLAGS, (byte)(b0 | id));
        } else {
            this.entityData.set(ID_FLAGS, (byte)(b0 & ~id));
        }
    }

    protected void setPickupItemStack(ItemStack pickupItemStack) {
        if (!pickupItemStack.isEmpty()) {
            this.pickupItemStack = pickupItemStack;
        } else {
            this.pickupItemStack = this.getDefaultPickupItem();
        }
    }

    public void setBaseDamageFromMob(float velocity) {
        this.setBaseDamage((double)(velocity * 2.0F) + this.random.triangle((double)this.level().getDifficulty().getId() * 0.11, 0.57425));
    }

    protected float getWaterInertia() {
        return 0.6F;
    }

    /**
     * Sets if this arrow can noClip
     */
    public void setNoPhysics(boolean noPhysics) {
        this.noPhysics = noPhysics;
        this.setFlag(2, noPhysics);
    }

    public boolean isNoPhysics() {
        return !this.level().isClientSide ? this.noPhysics : (this.entityData.get(ID_FLAGS) & 2) != 0;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !this.inGround;
    }

    // =====================================================================================================
    // Ball physics: heavy balls that skim, bounce and roll, then come to rest, instead of sticking in like
    // an arrow. Subclasses opt in with usesBallPhysics(); the maths lives in BallPhysics.
    //
    // Both sides run this: the server is in charge, and the client runs the same steps so the ball moves
    // smoothly between the server's updates (which come every tick). Hitting entities, detonating, coming
    // to rest and waking up again only happen on the server.
    // =====================================================================================================

    /** Furthest the ball moves between collision checks, so fast balls can't tunnel through or clip corners. */
    private static final double MAX_SWEEP_STEP = 0.25;
    private static final int MAX_SWEEP_STEPS = 48;
    /** Highest lip (carpet, snow layer, path or farmland edge) a rolling ball can roll up onto. */
    private static final double MAX_ROLL_STEP = 0.25;
    /** How far below a rolling ball we still count as ground, so it follows small dips instead of hopping. */
    private static final double GROUND_SNAP = 0.07;
    /** Sideways pull on a ball balanced on an edge with its centre over thin air. */
    private static final double EDGE_TIP_ACCELERATION = 0.015;
    /** A rolling ball has to run into a wall at least this fast for it to count as a hit. */
    private static final double WALL_HIT_SPEED = 0.03;
    /** A resting ball knocked faster than this (by an explosion, say) starts moving again. */
    private static final double WAKE_SPEED = 0.05;
    /** Ticks a ball has to be all but still before it rests. */
    private static final int TICKS_TO_REST = 3;
    private static final float LAVA_INERTIA = 0.5F;
    /** Closing speed below which a bounce is too soft to be heard. */
    private static final double SILENT_IMPACT_SPEED = 0.12;
    /** Closing speed at which a bounce is as loud and dusty as it gets. */
    private static final double FULL_IMPACT_SPEED = 1.5;
    /** Distance rolled between rumbles. */
    private static final float ROLL_SOUND_STRIDE = 0.9F;
    /** The speed trail is only drawn while a ball flies faster than this. */
    private static final double TRAIL_SPEED = 0.5;
    /** Fastest the ball model spins, in radians per tick, so it never strobes. */
    private static final float MAX_SPIN = 0.8F;
    private static final float AIR_SPIN_DECAY = 0.98F;

    private int slowTicks;

    // Client side only: how the ball model is turned while it tumbles and rolls
    private @Nullable Quaternionf spin;
    private final Quaternionf spinO = new Quaternionf();
    private final Vector3f spinAxis = new Vector3f(1.0F, 0.0F, 0.0F);
    private float spinRate;
    private float rolledSinceSound;

    /** Whether this projectile is a heavy ball that skims, bounces and rolls instead of sticking like an arrow. */
    public boolean usesBallPhysics() {
        return false;
    }

    /** The sound a ball makes on top of the block's own when it skims off something or hits something hard. */
    @Nullable
    protected SoundEvent ballClankSound() {
        return null;
    }

    /** Called on both sides after a ball hits a block, with what the hit did to it. */
    protected void onBallImpact(BlockHitResult hit, BlockState state, BallPhysics.Impact impact) {
        Level level = this.level();
        if (level.isClientSide) {
            // Friction at the contact point sets the ball spinning as if it rolled off the surface: about the axis
            // across its path, at the speed it's sliding divided by its radius (n x v only keeps the sliding part)
            Vec3 axis = BallPhysics.normalOf(hit.getDirection()).cross(impact.velocity());
            double sliding = axis.length();
            this.startSpinning();
            if (sliding > 1.0E-4) {
                this.spinAxis.set((float) (axis.x / sliding), (float) (axis.y / sliding), (float) (axis.z / sliding));
                this.spinRate = Math.min((float) sliding / this.ballRadius(), MAX_SPIN);
            }
            return;
        }

        double closing = impact.closingSpeed();
        if (closing < SILENT_IMPACT_SPEED) {
            return;
        }
        float strength = (float) Mth.clamp((closing - SILENT_IMPACT_SPEED) / (FULL_IMPACT_SPEED - SILENT_IMPACT_SPEED), 0.0, 1.0);
        Vec3 at = hit.getLocation();
        if (!this.isSilent()) {
            SoundType sound = state.getSoundType();
            level.playSound(null, at.x, at.y, at.z, sound.getFallSound(), this.getSoundSource(),
                    sound.getVolume() * (0.5F + strength), sound.getPitch() * 0.8F);
            SoundEvent clank = this.ballClankSound();
            if (clank != null && (impact.skimmed() || BallPhysics.Surface.of(state).ringsOnImpact())) {
                level.playSound(null, at.x, at.y, at.z, clank, this.getSoundSource(),
                        0.2F + 0.6F * strength, 1.1F + this.random.nextFloat() * 0.3F);
            }
        }
        if (level instanceof ServerLevel serverLevel && !state.isAir()) {
            // A puff of the block's dust, spread out along the face that was hit
            Direction.Axis axis = hit.getDirection().getAxis();
            double spreadX = axis == Direction.Axis.X ? 0.02 : 0.15;
            double spreadY = axis == Direction.Axis.Y ? 0.02 : 0.15;
            double spreadZ = axis == Direction.Axis.Z ? 0.02 : 0.15;
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), at.x, at.y, at.z,
                    3 + Mth.floor(strength * 15.0F), spreadX, spreadY, spreadZ, 0.15);
        }
    }

    /** Called on both sides every tick a ball is simulated, with how far it moved. */
    protected void onBallMoved(Vec3 moved) {
        if (!this.level().isClientSide || this.spin == null && !this.onGround()) {
            // Spin is only for show, and until its first touch the model keeps facing the way it flies
            return;
        }
        this.startSpinning();
        this.spinO.set(this.spin);
        if (this.onGround()) {
            double across = moved.horizontalDistance();
            if (across > 1.0E-4) {
                // Rolling without slipping: it turns by the distance over its radius, about the axis across its path
                this.spinAxis.set((float) moved.z, 0.0F, (float) -moved.x).normalize();
                this.spinRate = Math.min((float) across / this.ballRadius(), MAX_SPIN);
                this.rumble(across);
            } else {
                this.spinRate = 0.0F;
            }
        } else {
            this.spinRate *= AIR_SPIN_DECAY;
        }
        if (this.spinRate > 1.0E-4F) {
            this.spin.premul(new Quaternionf().rotationAxis(this.spinRate, this.spinAxis)).normalize();
        }
    }

    /** Called on both sides when a fast, flat ball skips off the surface of water. */
    protected void onBallSkippedOffWater(Vec3 at, double speed) {
        if (this.level() instanceof ServerLevel serverLevel) {
            float strength = (float) Mth.clamp(speed / 3.0, 0.2, 1.0);
            serverLevel.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.1, at.z, 6 + Mth.floor(strength * 18.0F), 0.3, 0.05, 0.3, 0.2);
            if (!this.isSilent()) {
                serverLevel.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_SPLASH, this.getSoundSource(),
                        0.4F + 0.5F * strength, 1.2F + this.random.nextFloat() * 0.3F);
            }
        }
    }

    /** Whether to draw the speed trail. Balls only show it while they're flying fast. */
    public boolean showsTrail() {
        return !this.usesBallPhysics() || !this.onGround() && this.getDeltaMovement().lengthSqr() > TRAIL_SPEED * TRAIL_SPEED;
    }

    /** How the ball model is turned while it tumbles and rolls, or null while it still faces the way it flies. Client only. */
    @Nullable
    public Quaternionf getSpin(float partialTicks) {
        return this.spin == null ? null : this.spinO.slerp(this.spin, partialTicks, new Quaternionf());
    }

    private void startSpinning() {
        if (this.spin == null) {
            // Carry on from the way the renderer turned the model in flight, so it doesn't snap round
            this.spin = new Quaternionf().rotationY((this.getYRot() - 90.0F) * Mth.DEG_TO_RAD).rotateZ((this.getXRot() + 90.0F) * Mth.DEG_TO_RAD);
            this.spinO.set(this.spin);
        }
    }

    private float ballRadius() {
        return Math.max(this.getBbWidth() * 0.5F, 0.05F);
    }

    /** The low rumble of a heavy ball rolling over the ground, using the ground's own step sound. Client only. */
    private void rumble(double distance) {
        this.rolledSinceSound += (float) distance;
        if (this.rolledSinceSound < ROLL_SOUND_STRIDE) {
            return;
        }
        this.rolledSinceSound = 0.0F;
        BlockState ground = this.level().getBlockState(this.getOnPos());
        if (ground.isAir() || this.isSilent()) {
            return;
        }
        SoundType sound = ground.getSoundType();
        float speed = (float) this.getDeltaMovement().horizontalDistance();
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), sound.getStepSound(), this.getSoundSource(),
                sound.getVolume() * Mth.clamp(speed * 1.2F, 0.08F, 0.45F), sound.getPitch() * 0.6F, false);
    }

    private void tickBallPhysics() {
        Vec3 motion = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F && motion.lengthSqr() > 1.0E-7) {
            this.setYRot((float)(Mth.atan2(motion.x, motion.z) * 180.0F / (float)Math.PI));
            this.setXRot((float)(Mth.atan2(motion.y, motion.horizontalDistance()) * 180.0F / (float)Math.PI));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        if (this.isInWaterOrRain() || this.level().getBlockState(this.blockPosition()).is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.inGround) {
            // At rest. The server decides when it gets going again; the client waits to hear about it.
            this.inGroundTime++;
            if (!this.level().isClientSide) {
                if (this.getDeltaMovement().lengthSqr() > WAKE_SPEED * WAKE_SPEED || !this.isSupported()) {
                    this.wakeUp();
                } else {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.tickDespawn();
                }
            }
            this.onBallMoved(Vec3.ZERO);
            return;
        }
        this.inGroundTime = 0;

        Vec3 start = this.position();
        if (this.onGround() && motion.y <= 0.0) {
            this.tickRolling(motion);
        } else {
            this.tickFlying(motion);
        }
        if (this.isRemoved()) {
            return;
        }
        this.checkInsideBlocks();
        this.onBallMoved(this.position().subtract(start));
    }

    private void tickFlying(Vec3 motion) {
        // Fast, flat balls skip across water like a stone
        if (!this.isInWater() && this.trySkipOffWater(motion)) {
            this.applyBallDrag();
            return;
        }

        // A ball that starts out inside a block (fired point blank into a wall) hits it straight away
        if (this.tickCount <= 2) {
            BlockHitResult inside = this.findBlockInside(this.getBoundingBox(), motion);
            if (inside != null) {
                this.hitBlock(inside, motion);
                if (!this.isRemoved()) {
                    this.applyBallDrag();
                }
                return;
            }
        }

        Sweep sweep = this.sweep(motion);
        Vec3 start = this.position();

        // Whatever is in the way gets hit first
        if (!this.level().isClientSide) {
            Vec3 from = this.getBoundingBox().getCenter();
            if (this.hitEntitiesAlong(from, from.add(sweep.moved()))) {
                if (!this.isRemoved()) {
                    this.applyBallDrag();
                }
                return;
            }
            if (this.isRemoved()) {
                return;
            }
        }

        // Smashing through something on the way slows the ball down before it reaches the block
        Vec3 incoming = this.getDeltaMovement();
        this.setPos(start.add(sweep.moved()));
        this.setOnGround(false);
        if (sweep.blockedX() || sweep.blockedY() || sweep.blockedZ()) {
            Direction.Axis main = null;
            if (sweep.hit() != null) {
                this.hitBlock(sweep.hit(), incoming);
                if (this.isRemoved()) {
                    return;
                }
                main = sweep.hit().getDirection().getAxis();
            }
            // A corner can block more than one axis at once: don't keep pushing into the other walls
            Vec3 after = this.getDeltaMovement();
            double x = sweep.blockedX() && main != Direction.Axis.X && after.x * motion.x > 0.0 ? -after.x * 0.3 : after.x;
            double y = sweep.blockedY() && main != Direction.Axis.Y && after.y * motion.y > 0.0 ? -after.y * 0.3 : after.y;
            double z = sweep.blockedZ() && main != Direction.Axis.Z && after.z * motion.z > 0.0 ? -after.z * 0.3 : after.z;
            this.setDeltaMovement(x, y, z);
        }
        this.applyBallDrag();
    }

    private void tickRolling(Vec3 motion) {
        Level level = this.level();
        Vec3 tip = this.edgeTip();
        Vec3 velocity = motion.add(tip);
        Vec3 start = this.position();
        AABB box = this.getBoundingBox();

        // Roll across in short steps, stopping at the first wall but rolling up small lips on the way
        Vec3 across = new Vec3(velocity.x, 0.0, velocity.z);
        int steps = Mth.clamp(Mth.ceil(across.length() / MAX_SWEEP_STEP), 1, MAX_SWEEP_STEPS);
        Vec3 step = across.scale(1.0 / steps);
        Vec3 moved = Vec3.ZERO;
        boolean blockedX = false;
        boolean blockedZ = false;
        for (int i = 0; i < steps; i++) {
            Vec3 allowed = Entity.collideBoundingBox(this, step, box, level, List.of());
            boolean x = isBlocked(step.x, allowed.x);
            boolean z = isBlocked(step.z, allowed.z);
            if (x || z) {
                Vec3 climbed = this.tryStepUp(box, step);
                if (climbed != null && climbed.horizontalDistanceSqr() > allowed.horizontalDistanceSqr() + 1.0E-8) {
                    // Climbing costs speed, and a ball that's too slow can't make it up at all
                    double speedSqr = velocity.horizontalDistanceSqr() - 2.0 * this.getGravity() * climbed.y;
                    if (speedSqr > 0.0) {
                        double scale = Math.sqrt(speedSqr / velocity.horizontalDistanceSqr());
                        velocity = new Vec3(velocity.x * scale, velocity.y, velocity.z * scale);
                        allowed = climbed;
                        x = isBlocked(step.x, climbed.x);
                        z = isBlocked(step.z, climbed.z);
                    }
                }
            }
            box = box.move(allowed);
            moved = moved.add(allowed);
            if (x || z) {
                blockedX = x;
                blockedZ = z;
                break;
            }
        }

        // Stay on the ground, following small dips, or roll off the edge and fall
        Vec3 drop = Entity.collideBoundingBox(this, new Vec3(0.0, -GROUND_SNAP, 0.0), box, level, List.of());
        boolean grounded = isBlocked(-GROUND_SNAP, drop.y);
        if (grounded) {
            moved = moved.add(drop);
        }

        // A rolling ball bowls into whoever is in its way, or bounces off their legs
        if (!level.isClientSide && moved.horizontalDistanceSqr() > 1.0E-6) {
            Vec3 from = this.getBoundingBox().getCenter();
            Vec3 before = this.getDeltaMovement();
            if (this.hitEntitiesAlong(from, from.add(moved))) {
                if (!this.isRemoved()) {
                    this.applyBallDrag();
                }
                return;
            }
            if (this.isRemoved()) {
                return;
            }
            if (!before.equals(this.getDeltaMovement())) {
                // Smashed straight through someone, which took some of its speed
                velocity = velocity.scale(this.getDeltaMovement().length() / Math.max(before.length(), 1.0E-9));
            }
        }
        this.setPos(start.add(moved));
        this.setOnGroundWithKnownMovement(grounded, moved);

        // Bumping into a wall
        if (blockedX || blockedZ) {
            double closing = Math.max(blockedX ? Math.abs(velocity.x) : 0.0, blockedZ ? Math.abs(velocity.z) : 0.0);
            BlockHitResult wall = this.contactWith(this.getBoundingBox(), step, blockedX, false, blockedZ);
            if (wall != null && closing > WALL_HIT_SPEED) {
                this.hitBlock(wall, velocity);
                if (this.isRemoved()) {
                    return;
                }
                velocity = this.getDeltaMovement();
            } else {
                velocity = new Vec3(blockedX ? 0.0 : velocity.x, velocity.y, blockedZ ? 0.0 : velocity.z);
            }
        }

        if (this.onGround()) {
            BallPhysics.Surface surface = BallPhysics.Surface.of(level.getBlockState(this.getOnPos()));
            velocity = BallPhysics.rollingResistance(velocity, surface);
            velocity = new Vec3(velocity.x, 0.0, velocity.z);
        }
        if (this.isInWater()) {
            velocity = velocity.scale(this.getWaterInertia());
        } else if (this.isInLava()) {
            velocity = velocity.scale(LAVA_INERTIA);
        }
        this.setDeltaMovement(velocity);
        this.applyGravity();

        if (!level.isClientSide) {
            if (this.onGround() && tip == Vec3.ZERO && velocity.horizontalDistance() < BallPhysics.REST_SPEED) {
                if (++this.slowTicks >= TICKS_TO_REST) {
                    this.comeToRest();
                }
            } else {
                this.slowTicks = 0;
            }
        }
    }

    /** Runs a block hit through the usual projectile hooks, which end up in {@link #bounceOffBlock}. */
    private void hitBlock(BlockHitResult hit, Vec3 incoming) {
        this.setDeltaMovement(incoming);
        if (net.luckystudios.platform.Services.PLATFORM.onProjectileImpact(this, hit)) {
            // Another mod cancelled the hit, but the ball still can't pass through the block
            this.bounceOffBlock(hit);
        } else {
            this.hitTargetOrDeflectSelf(hit);
        }
        this.hasImpulse = true;
    }

    /** The physical part of a block hit: bounce, skim or settle into rolling. */
    protected void bounceOffBlock(BlockHitResult hit) {
        Level level = this.level();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        // The block may already be gone (blown up by the ball's own blast): then there's nothing to bounce off
        if (state.getCollisionShape(level, pos, CollisionContext.of(this)).isEmpty()) {
            return;
        }
        BallPhysics.Impact impact = BallPhysics.collide(this.getDeltaMovement(), hit.getDirection(), BallPhysics.Surface.of(state), this.isInWater());
        this.setDeltaMovement(impact.velocity());
        if (impact.settled()) {
            this.setOnGround(true);
        }
        this.onBallImpact(hit, state, impact);
    }

    /** Whether a ball skims off this block instead of hitting it properly. Glass, leaves and snow get smashed instead. */
    protected boolean skimsOff(BlockHitResult hit) {
        if (!this.usesBallPhysics() || this.isInWater()) {
            return false;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = this.level().getBlockState(pos);
        float hardness = state.getDestroySpeed(this.level(), pos);
        if (hardness >= 0.0F && hardness < BallPhysics.FRAGILE_HARDNESS) {
            return false;
        }
        return BallPhysics.skims(this.getDeltaMovement(), hit.getDirection(), BallPhysics.Surface.of(state));
    }

    /** Air (or water, or lava) drag, then gravity, in the same order as the arrow-style flight. */
    private void applyBallDrag() {
        float inertia = this.isInWater() ? this.getWaterInertia() : this.isInLava() ? LAVA_INERTIA : 0.99F;
        this.setDeltaMovement(this.getDeltaMovement().scale(inertia));
        this.applyGravity();
    }

    private void comeToRest() {
        this.setDeltaMovement(Vec3.ZERO);
        this.inGround = true;
        this.lastState = this.level().getBlockState(this.getOnPos());
        this.slowTicks = 0;
        this.hasImpulse = true;
    }

    private void wakeUp() {
        this.inGround = false;
        this.setOnGround(false);
        this.slowTicks = 0;
        this.hasImpulse = true;
    }

    /** Whether something right under the ball is holding it up. */
    private boolean isSupported() {
        return !this.level().noCollision(this, this.getBoundingBox().move(0.0, -0.06, 0.0));
    }

    /** A ball whose centre hangs over thin air tips off towards the open side instead of balancing on the edge. */
    private Vec3 edgeTip() {
        AABB box = this.getBoundingBox();
        Vec3 c = box.getCenter();
        if (this.hasFloorUnder(c.x, c.z, box.minY)) {
            return Vec3.ZERO;
        }
        double reach = box.getXsize() * 0.4;
        double x = 0.0;
        double z = 0.0;
        if (this.hasFloorUnder(c.x + reach, c.z, box.minY)) x -= 1.0;
        if (this.hasFloorUnder(c.x - reach, c.z, box.minY)) x += 1.0;
        if (this.hasFloorUnder(c.x, c.z + reach, box.minY)) z -= 1.0;
        if (this.hasFloorUnder(c.x, c.z - reach, box.minY)) z += 1.0;
        if (x == 0.0 && z == 0.0) {
            // Only a corner is holding it up
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    if (this.hasFloorUnder(c.x + sx * reach, c.z + sz * reach, box.minY)) {
                        x -= sx;
                        z -= sz;
                    }
                }
            }
        }
        Vec3 push = new Vec3(x, 0.0, z);
        return push.lengthSqr() < 1.0E-9 ? Vec3.ZERO : push.normalize().scale(EDGE_TIP_ACCELERATION);
    }

    private boolean hasFloorUnder(double x, double z, double bottom) {
        return !this.level().noCollision(this, new AABB(x - 0.01, bottom - 0.05, z - 0.01, x + 0.01, bottom - 1.0E-4, z + 0.01));
    }

    /** Skips the ball off the surface of water if it's coming in fast and flat. */
    private boolean trySkipOffWater(Vec3 motion) {
        Vec3 skip = BallPhysics.skipOffWater(motion);
        if (skip == null) {
            return false;
        }
        Level level = this.level();
        Vec3 from = this.position();
        BlockHitResult hit = level.clip(new ClipContext(from, from.add(motion), ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, this));
        if (hit.getType() == HitResult.Type.MISS || hit.getDirection() != Direction.UP || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return false;
        }
        Sweep sweep = this.sweep(hit.getLocation().subtract(from));
        this.setPos(from.add(sweep.moved()));
        this.setDeltaMovement(skip);
        this.setOnGround(false);
        this.hasImpulse = true;
        this.onBallSkippedOffWater(this.position(), motion.length());
        return true;
    }

    /**
     * Hits whatever is between {@code from} and {@code to} (the ball's centre this tick), server side.
     * Returns true if something stopped or deflected the ball, which then ends the tick where it touched them.
     */
    private boolean hitEntitiesAlong(Vec3 from, Vec3 to) {
        for (int i = 0; i < 8 && !this.isRemoved(); i++) {
            EntityHitResult hit = this.findHitEntity(from, to);
            if (hit == null) {
                return false;
            }
            Entity target = hit.getEntity();
            boolean friendlyFire = target instanceof Player victim && this.getOwner() instanceof Player shooter && !shooter.canHarmPlayer(victim);
            if (friendlyFire || net.luckystudios.platform.Services.PLATFORM.onProjectileImpact(this, hit)) {
                this.ignoreEntity(target);
                continue;
            }
            Vec3 before = this.getDeltaMovement();
            this.hitTargetOrDeflectSelf(hit);
            this.hasImpulse = true;
            if (this.isRemoved()) {
                return true;
            }
            if (this.getDeltaMovement().dot(before) < before.lengthSqr() * 0.5) {
                // Knocked off course: stop where the ball touched them
                Vec3 touch = target.getBoundingBox().inflate(0.3).clip(from, to).orElse(from);
                Sweep sweep = this.sweep(touch.subtract(from));
                this.setPos(this.position().add(sweep.moved()));
                return true;
            }
            // Smashed straight through: look for the next one
        }
        return false;
    }

    /** A ball running into a body: hurts it if fast enough, then either smashes through or glances off. */
    private void ballHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        this.ignoreEntity(target);
        if (this.level().isClientSide) {
            return;
        }
        Vec3 velocity = this.getDeltaMovement();
        double speed = velocity.length();
        Entity owner = this.getOwner();
        boolean smashedThrough = false;
        if (speed >= BallPhysics.MIN_DAMAGE_SPEED) {
            DamageSource source = this.damageSources().source(ModDamageTypes.CANNON, owner != null ? owner : this);
            int damage = Mth.ceil(Mth.clamp(speed * this.baseDamage, 0.0, 2.147483647E9));
            if (owner instanceof LivingEntity livingOwner) {
                livingOwner.setLastHurtMob(target);
            }
            boolean enderman = target.getType() == EntityType.ENDERMAN;
            int fireTicks = target.getRemainingFireTicks();
            if (this.isOnFire() && !enderman) {
                target.igniteForSeconds(5.0F);
            }
            if (target.hurt(source, (float) damage)) {
                if (enderman) {
                    return; // It teleports away and the ball flies on
                }
                if (target instanceof LivingEntity living) {
                    this.doKnockback(living, source);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, living, source, this.getWeaponItem());
                    }
                    if (living != owner && living instanceof Player && owner instanceof ServerPlayer serverOwner && !this.isSilent()) {
                        serverOwner.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                    }
                }
                smashedThrough = !target.isAlive();
            } else {
                target.setRemainingFireTicks(fireTicks);
            }
        }
        if (smashedThrough) {
            this.setDeltaMovement(velocity.scale(BallPhysics.PLOW_THROUGH_KEEP));
        } else {
            this.setDeltaMovement(BallPhysics.glanceOff(velocity, this.getBoundingBox().getCenter(), target.getBoundingBox()));
            if (!(target instanceof Player) && target.isPushable()) {
                Vec3 shove = velocity.multiply(1.0, 0.0, 1.0).scale(0.15);
                target.push(shove.x, 0.0, shove.z);
            }
        }
    }

    private void ignoreEntity(Entity entity) {
        if (this.piercingIgnoreEntityIds == null) {
            this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
        }
        this.piercingIgnoreEntityIds.add(entity.getId());
    }

    /** Where the ball's box got to along a path, and the block face it ran into, if any. */
    private record Sweep(Vec3 moved, @Nullable BlockHitResult hit, boolean blockedX, boolean blockedY, boolean blockedZ) {}

    /** Slides the ball's box along {@code motion} in short steps and stops at the first thing it touches. */
    private Sweep sweep(Vec3 motion) {
        AABB box = this.getBoundingBox();
        int steps = Mth.clamp(Mth.ceil(motion.length() / MAX_SWEEP_STEP), 1, MAX_SWEEP_STEPS);
        Vec3 step = motion.scale(1.0 / steps);
        Vec3 moved = Vec3.ZERO;
        for (int i = 0; i < steps; i++) {
            Vec3 allowed = Entity.collideBoundingBox(this, step, box, this.level(), List.of());
            box = box.move(allowed);
            moved = moved.add(allowed);
            boolean x = isBlocked(step.x, allowed.x);
            boolean y = isBlocked(step.y, allowed.y);
            boolean z = isBlocked(step.z, allowed.z);
            if (x || y || z) {
                return new Sweep(moved, this.contactWith(box, step, x, y, z), x, y, z);
            }
        }
        return new Sweep(moved, null, false, false, false);
    }

    private static boolean isBlocked(double wanted, double got) {
        return Math.abs(wanted - got) > 1.0E-6;
    }

    /** Rolls the box up onto a low lip, if there's room. Returns the whole movement, or null if it can't. */
    @Nullable
    private Vec3 tryStepUp(AABB box, Vec3 step) {
        Level level = this.level();
        Vec3 up = Entity.collideBoundingBox(this, new Vec3(0.0, MAX_ROLL_STEP, 0.0), box, level, List.of());
        if (up.y <= 1.0E-4) {
            return null;
        }
        AABB raised = box.move(up);
        Vec3 over = Entity.collideBoundingBox(this, step, raised, level, List.of());
        Vec3 down = Entity.collideBoundingBox(this, new Vec3(0.0, -up.y, 0.0), raised.move(over), level, List.of());
        Vec3 total = up.add(over).add(down);
        return total.y > 1.0E-4 ? total : null;
    }

    /** The face of the block the ball's box just ran into, as a hit result. */
    @Nullable
    private BlockHitResult contactWith(AABB box, Vec3 step, boolean blockedX, boolean blockedY, boolean blockedZ) {
        // Of the blocked directions, the one the ball was moving fastest along is the face it hit
        Direction.Axis axis = Direction.Axis.Y;
        double fastest = -1.0;
        if (blockedX && Math.abs(step.x) > fastest) {
            axis = Direction.Axis.X;
            fastest = Math.abs(step.x);
        }
        if (blockedY && Math.abs(step.y) > fastest) {
            axis = Direction.Axis.Y;
            fastest = Math.abs(step.y);
        }
        if (blockedZ && Math.abs(step.z) > fastest) {
            axis = Direction.Axis.Z;
        }
        Direction face = faceAgainst(axis, axis.choose(step.x, step.y, step.z));
        double reach = 1.0E-3;
        AABB probe = switch (face) {
            case WEST -> new AABB(box.maxX, box.minY, box.minZ, box.maxX + reach, box.maxY, box.maxZ);
            case EAST -> new AABB(box.minX - reach, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
            case DOWN -> new AABB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY + reach, box.maxZ);
            case UP -> new AABB(box.minX, box.minY - reach, box.minZ, box.maxX, box.minY, box.maxZ);
            case NORTH -> new AABB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ + reach);
            case SOUTH -> new AABB(box.minX, box.minY, box.minZ - reach, box.maxX, box.maxY, box.minZ);
        };
        Vec3 c = box.getCenter();
        BlockPos pos = this.closestBlockIn(probe, c);
        if (pos == null) {
            return null; // Something that isn't a block, like the world border
        }
        Vec3 contact = switch (face) {
            case WEST -> new Vec3(box.maxX, c.y, c.z);
            case EAST -> new Vec3(box.minX, c.y, c.z);
            case DOWN -> new Vec3(c.x, box.maxY, c.z);
            case UP -> new Vec3(c.x, box.minY, c.z);
            case NORTH -> new Vec3(c.x, c.y, box.maxZ);
            case SOUTH -> new Vec3(c.x, c.y, box.minZ);
        };
        return new BlockHitResult(contact, face, pos, false);
    }

    /** A block the ball's box is already inside, as a hit on the face it was heading into. */
    @Nullable
    private BlockHitResult findBlockInside(AABB box, Vec3 motion) {
        BlockPos pos = this.closestBlockIn(box.deflate(1.0E-3), box.getCenter());
        if (pos == null) {
            return null;
        }
        Direction.Axis axis = Direction.Axis.Y;
        if (Math.abs(motion.x) > Math.abs(motion.y) && Math.abs(motion.x) >= Math.abs(motion.z)) {
            axis = Direction.Axis.X;
        } else if (Math.abs(motion.z) > Math.abs(motion.y)) {
            axis = Direction.Axis.Z;
        }
        return new BlockHitResult(box.getCenter(), faceAgainst(axis, axis.choose(motion.x, motion.y, motion.z)), pos, true);
    }

    /** The block whose collision shape overlaps {@code probe} closest to {@code center}. */
    @Nullable
    private BlockPos closestBlockIn(AABB probe, Vec3 center) {
        Level level = this.level();
        CollisionContext context = CollisionContext.of(this);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(probe.minX), Mth.floor(probe.minY), Mth.floor(probe.minZ),
                Mth.floor(probe.maxX), Mth.floor(probe.maxY), Mth.floor(probe.maxZ))) {
            VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos, context);
            if (shape.isEmpty()) {
                continue;
            }
            for (AABB part : shape.toAabbs()) {
                AABB placed = part.move(pos);
                if (!placed.intersects(probe)) {
                    continue;
                }
                double dx = Math.max(0.0, Math.max(placed.minX - center.x, center.x - placed.maxX));
                double dy = Math.max(0.0, Math.max(placed.minY - center.y, center.y - placed.maxY));
                double dz = Math.max(0.0, Math.max(placed.minZ - center.z, center.z - placed.maxZ));
                double distance = dx * dx + dy * dy + dz * dz;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    /** The face a box moving along {@code axis} (in the direction of {@code movement}'s sign) runs into. */
    private static Direction faceAgainst(Direction.Axis axis, double movement) {
        return switch (axis) {
            case X -> movement > 0.0 ? Direction.WEST : Direction.EAST;
            case Y -> movement > 0.0 ? Direction.DOWN : Direction.UP;
            case Z -> movement > 0.0 ? Direction.NORTH : Direction.SOUTH;
        };
    }

    public enum Pickup {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;

        public static AbstractNewProjectile.Pickup byOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal > values().length) {
                ordinal = 0;
            }

            return values()[ordinal];
        }
    }
}
