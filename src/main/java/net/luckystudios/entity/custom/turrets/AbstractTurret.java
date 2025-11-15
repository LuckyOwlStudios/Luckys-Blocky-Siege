package net.luckystudios.entity.custom.turrets;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.Set;

public abstract class AbstractTurret extends Mob {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int checkInterval;
    protected BlockPos pos;

    protected AbstractTurret(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    protected AbstractTurret(EntityType<? extends Mob> entityType, Level level, BlockPos pos) {
        this(entityType, level);
        this.pos = pos;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.checkBelowWorld();
            if (this.checkInterval++ == 100) {
                this.checkInterval = 0;
                if (!this.isRemoved() && !this.survives()) {
                    this.discard();
                    this.level().addAlwaysVisibleParticle(breakParticle(), true,
                        this.getX(), this.getY(), this.getZ(),
                        0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    public boolean survives() {
        return this.level().getBlockState(blockPosition().below()).is(this.attachedBlockState().getBlock());
    }

    public abstract BlockState attachedBlockState();

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        BlockPos blockpos = this.getPos();
        compound.putInt("TileX", blockpos.getX());
        compound.putInt("TileY", blockpos.getY());
        compound.putInt("TileZ", blockpos.getZ());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        BlockPos blockpos = new BlockPos(compound.getInt("TileX"), compound.getInt("TileY"), compound.getInt("TileZ"));
        if (!blockpos.closerThan(this.blockPosition(), 16.0)) {
            LOGGER.error("Block-attached entity at invalid position: {}", blockpos);
        } else {
            this.pos = blockpos;
        }
    }

    public abstract ParticleOptions breakParticle();

//    /**
//     * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
//     */
//    @Override
//    public void setPos(double x, double y, double z) {
//        this.pos = BlockPos.containing(x, y, z);
//        this.hasImpulse = true;
//    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    public void setDeltaMovement(Vec3 deltaMovement) {

    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isAlive();
    }

    @Override
    public boolean teleportTo(ServerLevel level, double x, double y, double z, Set<RelativeMovement> relativeMovements, float yRot, float xRot) {
        return false;
    }
}
