package net.luckystudios.entity.custom.cannon_ball;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public abstract class AbstractCannonBall extends AbstractNewProjectile {

    protected AbstractCannonBall(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
    }

    protected AbstractCannonBall(EntityType<? extends AbstractNewProjectile> entityType, double x, double y, double z, Level level, ItemStack pickupItemStack) {
        super(entityType, x, y, z, level, pickupItemStack);
        this.setBaseDamage(baseDamage());
        this.pickup = Pickup.ALLOWED;
    }

    protected AbstractCannonBall(EntityType<? extends AbstractNewProjectile> entityType, LivingEntity owner, Level level, ItemStack pickupItemStack) {
        super(entityType, owner, level, pickupItemStack);
        this.setBaseDamage(baseDamage());
        this.pickup = Pickup.ALLOWED;
    }

    protected abstract List<ParticleOptions> getTrailParticles();

    protected abstract float baseDamage();

    protected abstract SoundEvent impactSound();

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide) {
            if (!this.inGround) {
                for (ParticleOptions particle : getTrailParticles()) {
                    level.addParticle(particle, getX(), getY(), getZ(), 0, 0, 0);
                }
            }
        } else if (this.inGround && this.inGroundTime != 0 && this.inGroundTime >= 600) {
            level.broadcastEntityEvent(this, (byte)0);
            this.setPickupItemStack(new ItemStack(Items.ARROW));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), impactSound(), this.getSoundSource(), 1.0F, 1.0F);
        explode();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        explode();
    }

    public abstract void explode();
}
