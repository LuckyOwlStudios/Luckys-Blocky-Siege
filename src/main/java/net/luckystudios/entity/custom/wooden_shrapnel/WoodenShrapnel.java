package net.luckystudios.entity.custom.wooden_shrapnel;

import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.items.ModItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class WoodenShrapnel extends ThrowableItemProjectile {

    public WoodenShrapnel(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public WoodenShrapnel(Level level, double x, double y, double z) {
        super(ModEntityTypes.WOODEN_SHRAPNEL.get(), x, y, z, level);
    }

    public WoodenShrapnel(LivingEntity shooter, Level level) {
        super(ModEntityTypes.WOODEN_SHRAPNEL.get(), shooter, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.WOODEN_SHRAPNEL.asItem();
    }

    /**
     * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItem();
        // Change to BlockParticleOption with stone block
        return !itemstack.isEmpty() && !itemstack.is(this.getDefaultItem())
                ? new ItemParticleOption(ParticleTypes.ITEM, itemstack)
                : new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BARREL.defaultBlockState());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), 8.0F);
        rockHit();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        rockHit();
        this.discard();
    }

    private void rockHit() {
        this.level().playSound(this, this.blockPosition(), Blocks.BARREL.defaultBlockState().getSoundType(this.level(), this.blockPosition(), null).getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        this.level().broadcastEntityEvent(this, (byte)3);
    }
}
