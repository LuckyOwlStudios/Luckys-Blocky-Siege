package net.luckystudios.entity.custom.block_projectile;

import net.luckystudios.entity.custom.block_projectile.behavior.BlockProjectileBehavior;
import net.luckystudios.entity.custom.block_projectile.behavior.BlockProjectileBehaviorManager;
import net.luckystudios.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Any block item fired out of a cannon. On impact it either runs a data driven
 * {@link BlockProjectileBehavior} for that block, or tries to place itself where it landed.
 */
public class BlockProjectile extends Projectile {

    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE = SynchedEntityData.defineId(BlockProjectile.class, EntityDataSerializers.BLOCK_STATE);
    private static final float GRAVITY = 0.04F;
    private static final int MAX_LIFETIME = 600;

    // The stack keeps any block entity data / components so placing it restores them
    private ItemStack stack = ItemStack.EMPTY;

    public BlockProjectile(EntityType<? extends BlockProjectile> type, Level level) {
        super(type, level);
    }

    public BlockProjectile(Level level, double x, double y, double z, ItemStack stack) {
        this(ModEntityTypes.BLOCK_PROJECTILE.get(), level);
        this.setPos(x, y, z);
        ItemStack single = stack.copyWithCount(1);
        this.stack = single;
        if (single.getItem() instanceof BlockItem blockItem) {
            this.entityData.set(DATA_BLOCK_STATE, blockItem.getBlock().defaultBlockState());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_STATE, Blocks.STONE.defaultBlockState());
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && (this.tickCount > MAX_LIFETIME || this.getY() < this.level().getMinBuildHeight() - 32)) {
            this.dropAsItem();
            return;
        }

        Vec3 movement = this.getDeltaMovement();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(hit);
            if (this.isRemoved()) return;
        }

        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        float drag = this.isInWater() ? 0.8F : 0.99F;
        this.setDeltaMovement(movement.scale(drag).add(0, -GRAVITY, 0));
        this.updateRotation();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 location = result.getLocation();
        Optional<BlockProjectileBehavior> behavior = BlockProjectileBehaviorManager.getBehavior(this.getBlockState());
        if (behavior.isPresent()) {
            behavior.get().onImpact(serverLevel, location, this);
            this.discard();
            return;
        }

        if (!this.tryPlace(serverLevel, result)) {
            this.dropAsItem();
        } else {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Entity target = result.getEntity();
        DamageSource source = this.damageSources().thrown(this, this.getOwner());
        target.hurt(source, 4.0F);

        Optional<BlockProjectileBehavior> behavior = BlockProjectileBehaviorManager.getBehavior(this.getBlockState());
        if (behavior.isPresent()) {
            behavior.get().onImpact(serverLevel, result.getLocation(), this);
            this.discard();
        } else {
            // Can't place a block on a mob, so it drops
            this.dropAsItem();
        }
    }

    private boolean tryPlace(ServerLevel level, BlockHitResult result) {
        if (!(this.stack.getItem() instanceof BlockItem blockItem)) return false;
        // Nudge the hit point back out of the surface so the placement context targets the face we struck
        BlockHitResult placeHit = new BlockHitResult(result.getLocation(), result.getDirection(), result.getBlockPos(), false);
        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(level, null, InteractionHand.MAIN_HAND, this.stack.copy(), placeHit));
        InteractionResult placed = blockItem.place(context);
        return placed.consumesAction();
    }

    private void dropAsItem() {
        if (this.level() instanceof ServerLevel level && !this.stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), this.stack.copy());
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.stack.isEmpty()) {
            tag.put("Stack", this.stack.save(this.registryAccess()));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Stack")) {
            this.stack = ItemStack.parse(this.registryAccess(), tag.getCompound("Stack")).orElse(ItemStack.EMPTY);
            if (this.stack.getItem() instanceof BlockItem blockItem) {
                this.entityData.set(DATA_BLOCK_STATE, blockItem.getBlock().defaultBlockState());
            }
        }
    }
}
