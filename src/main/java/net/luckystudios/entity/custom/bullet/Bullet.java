package net.luckystudios.entity.custom.bullet;

import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.init.ModDamageTypes;
import net.luckystudios.init.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class Bullet extends ThrowableItemProjectile {

    public Bullet(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public Bullet(Level level, double x, double y, double z) {
        super(ModEntityTypes.BULLET.get(), x, y, z, level);
    }

    public Bullet(LivingEntity shooter, Level level) {
        super(ModEntityTypes.BULLET.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BULLET.asItem();
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide) {
            level.addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        entity.hurt(new DamageSource(level().holderOrThrow(ModDamageTypes.BULLET)), 6);
    }

    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }

    }
}
