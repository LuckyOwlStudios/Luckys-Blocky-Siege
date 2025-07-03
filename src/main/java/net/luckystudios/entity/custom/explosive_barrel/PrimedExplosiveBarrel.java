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
                explode(this.level(), this.getOwner(), this.position());
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    public static void explode(Level level, Entity owner, Vec3 position) {
//        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
//                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 4.0F, 1.5F);
        level.playSound(null, position.x, position.y, position.z,
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 4.0F, 0.75F);
        // Spread Shrapnel Projectiles all around the explosion
        if (!level.isClientSide()) {
            int shrapnelCount = 24; // Number of projectiles
            double centerX = position.x;
            double centerY = position.y; // Slightly above ground
            double centerZ = position.z;

            for (int i = 0; i < shrapnelCount; i++) {
                // Random angle in radians around the circle
                Random random = new Random();
                double angle = (2 * Math.PI / shrapnelCount) * i;
                double speed = 0.5 + random.nextDouble() * 0.3; // Slight randomness to speed

                double dx = Math.cos(angle) * speed;
                double dz = Math.sin(angle) * speed;
                double dy = (random.nextDouble() - 0.5) * 0.4; // Slight up/down variation

                WoodenShrapnel shrapnel = new WoodenShrapnel(level, centerX, centerY, centerZ);
                shrapnel.setDeltaMovement(dx, dy, dz);
                if (owner instanceof LivingEntity livingOwner) {
                    shrapnel.setOwner(livingOwner);
                }
                level.addFreshEntity(shrapnel);
            }
        }
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
