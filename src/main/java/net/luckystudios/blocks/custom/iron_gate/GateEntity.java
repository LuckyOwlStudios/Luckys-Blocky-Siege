package net.luckystudios.blocks.custom.iron_gate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GateEntity extends BlockAttachedEntity {

    public GateEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level) {
        super(entityType, level);
    }

    public GateEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level, BlockPos pos) {
        super(entityType, level, pos);
    }

    @Override
    protected void recalculateBoundingBox() {
    }

    protected AABB calculateBoundingBox(BlockPos pos, Direction p_direction) {
        Vec3 vec3 = Vec3.atCenterOf(pos).relative(p_direction, -0.46875F);
        double d0 = 3;
        double d1 = 5;
        Direction direction = p_direction.getCounterClockWise();
        Vec3 vec31 = vec3.relative(direction, d0).relative(Direction.UP, d1);
        Direction.Axis direction$axis = p_direction.getAxis();
        double d2 = direction$axis == Direction.Axis.X ? 1 : 3;
        double d3 = 5;
        double d4 = direction$axis == Direction.Axis.Z ? 1 : 3;
        return AABB.ofSize(vec31, d2, d3, d4);
    }

    @Override
    public boolean survives() {
        return false;
    }

    @Override
    public void dropItem(@Nullable Entity entity) {

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }
}
