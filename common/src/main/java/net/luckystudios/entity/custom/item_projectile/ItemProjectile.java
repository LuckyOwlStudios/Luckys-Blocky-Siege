package net.luckystudios.entity.custom.item_projectile;

import net.luckystudios.entity.custom.ImprovedProjectileEntity;
import net.luckystudios.init.ModEntityTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Generic projectile that flies as whatever item it carries and drops that item when it lands.
 * Used by the burst cannon for items that have no projectile of their own.
 */
public class ItemProjectile extends ImprovedProjectileEntity {

    public ItemProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ItemProjectile(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.ITEM_PROJECTILE.get(), x, y, z, level);
        this.setItem(stack.copyWithCount(1));
    }

    public ItemProjectile(LivingEntity shooter, Level level) {
        super(ModEntityTypes.ITEM_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STICK;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
            this.dropItemAndDiscard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.dropItemAndDiscard();
        }
    }

    private void dropItemAndDiscard() {
        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            this.spawnAtLocation(stack.copy());
        }
        this.discard();
    }
}
