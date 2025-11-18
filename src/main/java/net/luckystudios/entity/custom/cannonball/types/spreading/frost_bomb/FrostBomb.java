package net.luckystudios.entity.custom.cannonball.types.spreading.frost_bomb;

import net.luckystudios.entity.custom.cannonball.types.spreading.SpreadingBomb;
import net.luckystudios.entity.custom.liquid_projectile.AbstractLiquidProjectile;
import net.luckystudios.entity.custom.liquid_projectile.SnowProjectile;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.entity.custom.cannonball.AbstractNewProjectile;
import net.luckystudios.init.ModSoundEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;

public class FrostBomb extends SpreadingBomb {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            false, true, Optional.of(.25F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    public FrostBomb(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public AbstractLiquidProjectile spreadingProjectile() {
        return new SnowProjectile(level(), getX(), getY(), getZ());
    }

    @Override
    public Block blockToSpread() {
        return ModBlocks.FROST_PILE.get();
    }

    @Override
    public ParticleOptions particle() {
        return ParticleTypes.CLOUD;
    }

    public FrostBomb(Level level, double x, double y, double z) {
        super(ModEntityTypes.FROST_BOMB.get(), level, x, y, z, ModBlocks.FROST_BOMB.toStack());
    }

    public FrostBomb(Level level, LivingEntity owner) {
        super(ModEntityTypes.FROST_BOMB.get(), level, owner, ModBlocks.FROST_BOMB.toStack());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModBlocks.FROST_BOMB.asItem().getDefaultInstance();
    }

    @Override
    protected float baseDamage() {
        return 8;
    }

    @Override
    protected SoundEvent impactSound() {
        return SoundEvents.GLASS_BREAK;
    }

    @Override
    public void spawnTrailParticles() {
        super.spawnTrailParticles();
        Vec3 newPos = this.position();
        if (this.tickCount > 1 && !this.isInWater()) {

            double dx = newPos.x - xo;
            double dy = newPos.y - yo;
            double dz = newPos.z - zo;
            int s = 4;
            for (int i = 0; i < s; ++i) {
                double j = i / (double) s;
                this.level().addParticle(ParticleTypes.CLOUD,
                        xo - dx * j,
                        0.25 + yo - dy * j,
                        zo - dz * j,
                        0, 0.02, 0);
                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        xo - dx * j,
                        0.25 + yo - dy * j,
                        zo - dz * j,
                        0, 0.02, 0);
            }
        }
    }
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        explode();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        explode();
    }

    public void explode() {
        super.explode();
        this.level()
                .explode(
                        this,
                        null,
                        EXPLOSION_DAMAGE_CALCULATOR,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        2F,
                        true,
                        Level.ExplosionInteraction.TRIGGER,
                        ParticleTypes.CLOUD,
                        ParticleTypes.SNOWFLAKE,
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSoundEvents.IMPACT_ICY.get())
                );
        this.discard();
    }
}
