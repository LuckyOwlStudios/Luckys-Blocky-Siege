package net.luckystudios.entity.custom.bullet;

import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.items.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide) {
            level.addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        this.discard();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BULLET.asItem();
    }
}
