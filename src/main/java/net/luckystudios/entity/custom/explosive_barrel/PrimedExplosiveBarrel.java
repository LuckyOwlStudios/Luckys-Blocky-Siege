package net.luckystudios.entity.custom.explosive_barrel;

import net.luckystudios.init.ModBlocks;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.entity.custom.wooden_shrapnel.WoodenShrapnel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class PrimedExplosiveBarrel extends Entity implements TraceableEntity {

    private static int projectileCount = 32;

    private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(PrimedExplosiveBarrel.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(PrimedExplosiveBarrel.class, EntityDataSerializers.BLOCK_STATE);
    private static final ExplosionDamageCalculator USED_PORTAL_DAMAGE_CALCULATOR = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldBlockExplode(Explosion p_353087_, BlockGetter p_353096_, BlockPos p_353092_, BlockState p_353086_, float p_353094_) {
            return p_353086_.is(Blocks.NETHER_PORTAL) ? false : super.shouldBlockExplode(p_353087_, p_353096_, p_353092_, p_353086_, p_353094_);
        }

        @Override
        public Optional<Float> getBlockExplosionResistance(
                Explosion p_353090_, BlockGetter p_353088_, BlockPos p_353091_, BlockState p_353093_, FluidState p_353095_
        ) {
            return p_353093_.is(Blocks.NETHER_PORTAL)
                    ? Optional.empty()
                    : super.getBlockExplosionResistance(p_353090_, p_353088_, p_353091_, p_353093_, p_353095_);
        }
    };
    @javax.annotation.Nullable
    private LivingEntity owner;
    private boolean usedPortal;

    public PrimedExplosiveBarrel(EntityType<? extends PrimedExplosiveBarrel> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public PrimedExplosiveBarrel(Level level, double x, double y, double z, @javax.annotation.Nullable LivingEntity owner) {
        this(ModEntityTypes.PRIMED_EXPLOSIVE_BARREL.get(), level);
        this.setPos(x, y, z);
        double d0 = level.random.nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(d0) * 0.02, 0.2F, -Math.cos(d0) * 0.02);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.owner = owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FUSE_ID, 80);
        builder.define(DATA_BLOCK_STATE_ID, ModBlocks.EXPLOSIVE_BARREL.get().defaultBlockState());
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        this.handlePortal();
        this.applyGravity();
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
        }

        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0) {
            this.discard();
            if (!this.level().isClientSide) {
                // Pass the current velocity to the explosion for directional spread
                explode(this.level(), this.getOwner(), this.position(), this.getDeltaMovement());
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    public static void explode(Level level, Entity owner, Vec3 position, Vec3 velocity) {
        level.playSound(null, position.x, position.y, position.z,
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 4.0F, 0.75F);

        if (!level.isClientSide()) {
            Random random = new Random();
            double centerX = position.x;
            double centerY = position.y;
            double centerZ = position.z;

            // Calculate movement speed to determine spread pattern
            double movementSpeed = velocity.length();
            boolean isMoving = velocity.x > 0.1 || velocity.z > 0.1; // Threshold for considering the barrel "moving"

            if (isMoving) {
                // Create a cone spread in the direction of movement
                createConeSpread(level, owner, centerX, centerY, centerZ, velocity, movementSpeed, random);
            } else {
                // Create a full circle spread for stationary barrels
                createCircleSpread(level, owner, centerX, centerY, centerZ, random);
            }
        }
    }

    private static void createConeSpread(Level level, Entity owner, double centerX, double centerY, double centerZ,
                                         Vec3 velocity, double movementSpeed, Random random) {
        int shrapnelCount = PrimedExplosiveBarrel.projectileCount; // More projectiles for dramatic effect
        double coneAngle = Math.PI / 3; // 60-degree cone (30 degrees each side)

        // Normalize the velocity to get direction
        Vec3 direction = velocity.normalize();
        double baseAngle = Math.atan2(direction.z, direction.x);

        // Main cone spread
        for (int i = 0; i < shrapnelCount; i++) {
            // Spread angles within the cone
            double spreadAngle = (random.nextDouble() - 0.5) * coneAngle;
            double finalAngle = baseAngle + spreadAngle;

            // Variable speed with bias toward movement direction
            double baseSpeed = 0.6 + random.nextDouble() * 0.4;
            double speedMultiplier = 1.0 + Math.cos(spreadAngle) * 0.5; // Faster in center of cone
            double speed = baseSpeed * speedMultiplier;

            // Calculate projectile velocity
            double dx = Math.cos(finalAngle) * speed;
            double dz = Math.sin(finalAngle) * speed;
            double dy = (random.nextDouble() - 0.3) * 0.6; // Slight upward bias

            // Add some inheritance from the barrel's momentum
            dx += direction.x * movementSpeed * 0.3;
            dy += direction.y * movementSpeed * 0.3;
            dz += direction.z * movementSpeed * 0.3;

            WoodenShrapnel shrapnel = new WoodenShrapnel(level, centerX, centerY, centerZ);
            shrapnel.setDeltaMovement(dx, dy, dz);
            if (owner instanceof LivingEntity livingOwner) {
                shrapnel.setOwner(livingOwner);
            }
            level.addFreshEntity(shrapnel);
        }

        // Add some backward projectiles for realism (but fewer and slower)
        int backwardCount = 8;
        for (int i = 0; i < backwardCount; i++) {
            double backwardAngle = baseAngle + Math.PI + (random.nextDouble() - 0.5) * Math.PI / 2;
            double speed = 0.2 + random.nextDouble() * 0.2; // Slower backward projectiles

            double dx = Math.cos(backwardAngle) * speed;
            double dz = Math.sin(backwardAngle) * speed;
            double dy = random.nextDouble() * 0.3;

            WoodenShrapnel shrapnel = new WoodenShrapnel(level, centerX, centerY, centerZ);
            shrapnel.setDeltaMovement(dx, dy, dz);
            if (owner instanceof LivingEntity livingOwner) {
                shrapnel.setOwner(livingOwner);
            }
            level.addFreshEntity(shrapnel);
        }
    }

    private static void createCircleSpread(Level level, Entity owner, double centerX, double centerY, double centerZ,
                                           Random random) {
        int shrapnelCount = 28; // Even distribution around circle

        for (int i = 0; i < shrapnelCount; i++) {
            // Evenly distributed angles with some randomness
            double baseAngle = (2 * Math.PI / shrapnelCount) * i;
            double angleVariation = (random.nextDouble() - 0.5) * 0.2; // Small random variation
            double angle = baseAngle + angleVariation;

            // Variable speed for more natural look
            double speed = 0.4 + random.nextDouble() * 0.5;

            double dx = Math.cos(angle) * speed;
            double dz = Math.sin(angle) * speed;
            double dy = (random.nextDouble() - 0.4) * 0.5; // Slight upward bias

            WoodenShrapnel shrapnel = new WoodenShrapnel(level, centerX, centerY, centerZ);
            shrapnel.setDeltaMovement(dx, dy, dz);
            if (owner instanceof LivingEntity livingOwner) {
                shrapnel.setOwner(livingOwner);
            }
            level.addFreshEntity(shrapnel);
        }
    }

    // Legacy method for backward compatibility
    public static void explode(Level level, Entity owner, Vec3 position) {
        explode(level, owner, position, Vec3.ZERO);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putShort("fuse", (short)this.getFuse());
        compound.put("block_state", NbtUtils.writeBlockState(this.getBlockState()));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.setFuse(compound.getShort("fuse"));
        if (compound.contains("block_state", 10)) {
            this.setBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compound.getCompound("block_state")));
        }
    }

    @javax.annotation.Nullable
    public LivingEntity getOwner() {
        return this.owner;
    }

    /**
     * Prepares this entity in new dimension by copying NBT data from entity in old dimension
     */
    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof PrimedExplosiveBarrel primedExplosiveBarrel) {
            this.owner = primedExplosiveBarrel.owner;
        }
    }

    public void setFuse(int life) {
        this.entityData.set(DATA_FUSE_ID, life);
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE_ID);
    }

    public void setBlockState(BlockState blockState) {
        this.entityData.set(DATA_BLOCK_STATE_ID, blockState);
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE_ID);
    }

    private void setUsedPortal(boolean usedPortal) {
        this.usedPortal = usedPortal;
    }

    @Nullable
    @Override
    public Entity changeDimension(DimensionTransition transition) {
        Entity entity = super.changeDimension(transition);
        if (entity instanceof PrimedExplosiveBarrel primedExplosiveBarrel) {
            primedExplosiveBarrel.setUsedPortal(true);
        }

        return entity;
    }
}
