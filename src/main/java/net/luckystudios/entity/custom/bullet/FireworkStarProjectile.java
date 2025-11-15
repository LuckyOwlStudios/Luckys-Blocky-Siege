package net.luckystudios.entity.custom.bullet;

import it.unimi.dsi.fastutil.ints.IntList;
import net.luckystudios.entity.custom.ImprovedProjectileEntity;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.init.ModDamageTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class FireworkStarProjectile extends ImprovedProjectileEntity {
    private static final EntityDataAccessor<ItemStack> FIREWORK_STAR_ITEM = SynchedEntityData.defineId(FireworkStarProjectile.class, EntityDataSerializers.ITEM_STACK);
    protected Vec3 movementOld;
    public FireworkStarProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FireworkStarProjectile(Level level, double x, double y, double z, ItemStack fireworkStarStack) {
        super(ModEntityTypes.FIREWORK_STAR.get(), x, y, z, level);
        this.entityData.set(FIREWORK_STAR_ITEM, fireworkStarStack.copy());
        this.movementOld = this.getDeltaMovement();
    }

    public FireworkStarProjectile(LivingEntity shooter, Level level) {
        super(ModEntityTypes.FIREWORK_STAR.get(), shooter, level);
        this.movementOld = this.getDeltaMovement();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FIREWORK_STAR_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIREWORK_STAR;
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 vec3 = this.getMovementToShoot(x, y, z, velocity, inaccuracy);
        this.setDeltaMovement(vec3);
        this.hasImpulse = true;
        double d0 = vec3.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)180.0F / (double)(float)Math.PI));
        this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)180.0F / (double)(float)Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void spawnTrailParticles() {
        if (this.isStuck) return;
        ItemStack fireChargeStack = this.getEntityData().get(FIREWORK_STAR_ITEM);
        FireworkExplosion fireworkExplosion = fireChargeStack.get(DataComponents.FIREWORK_EXPLOSION);
        handleTrail(fireworkExplosion);
    }

    private void handleTrail(FireworkExplosion fireworkExplosion) {
        Vec3 newPos = this.position();
        int color;
        // Try to get firework explosion from the item if we don't have it yet
        if (fireworkExplosion == null) {
            ItemStack itemStack = this.getItem();
            if (itemStack.has(DataComponents.FIREWORK_EXPLOSION)) {
                fireworkExplosion = itemStack.get(DataComponents.FIREWORK_EXPLOSION);
            }
        }

        if (fireworkExplosion != null && !fireworkExplosion.colors().isEmpty()) {
            IntList colorList = fireworkExplosion.colors();
            color = colorList.getInt(random.nextInt(colorList.size()));
        } else {
            // Default gray color when no firework explosion data
            color = 8421504; // Gray instead of black
        }

        Vector3f colorToVec3f = Vec3.fromRGB24(color).toVector3f();

        if (this.tickCount > 1) {

            double dx = newPos.x - xo;
            double dy = newPos.y - yo;
            double dz = newPos.z - zo;
            int s = 4;
            for (int i = 0; i < s; ++i) {
                double j = i / (double) s;
                this.level().addParticle(new DustParticleOptions(colorToVec3f, 1),
                        xo - dx * j,
                        0.25 + yo - dy * j,
                        zo - dz * j,
                        0, 0.02, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            Entity entity = result.getEntity();
            entity.hurt(new DamageSource(level().holderOrThrow(ModDamageTypes.BULLET)), 6);
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide()) {
            BlockState blockState = this.level().getBlockState(result.getBlockPos());
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }
}
