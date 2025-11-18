package net.luckystudios.entity.custom.cannonball.types.spreading.fire_bomb;

import net.luckystudios.entity.custom.cannonball.types.spreading.SpreadingBomb;
import net.luckystudios.entity.custom.ember.Ember;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.entity.custom.cannonball.AbstractNewProjectile;
import net.luckystudios.init.ModParticleTypes;
import net.luckystudios.init.ModSoundEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;

public class FireBomb extends SpreadingBomb {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, true, Optional.of(.25F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    public FireBomb(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FireBomb(Level level, double x, double y, double z) {
        super(ModEntityTypes.FIRE_BOMB.get(), level, x, y, z, ModBlocks.FIRE_BOMB.toStack());
    }

    public FireBomb(Level level, LivingEntity owner) {
        super(ModEntityTypes.FIRE_BOMB.get(), level, owner, ModBlocks.FIRE_BOMB.toStack());
    }


    @Override
    public Projectile spreadingProjectile() {
        return new Ember(level(), getX(), getY(), getZ());
    }

    @Override
    public Block blockToSpread() {
        return Blocks.FIRE;
    }

    @Override
    public ParticleOptions particle() {
        return ParticleTypes.FLAME;
    }


    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModBlocks.FIRE_BOMB.asItem().getDefaultInstance();
    }

    @Override
    public void tick() {
        super.tick();
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

                this.level().addParticle(ModParticleTypes.FLAME_TRAIL.get(),
                        xo - dx * j,
                        0.25 + yo - dy * j,
                        zo - dz * j,
                        0, 0.02, 0);
            }
        }
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
                        ParticleTypes.LAVA,
                        ParticleTypes.FLAME,
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSoundEvents.IMPACT_FIERY.get())
                );
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        switch (id) {
            case 67:
                RandomSource random = level().getRandom();
                for (int i = 0; i < 10; ++i) {
                    level().addParticle(ParticleTypes.SMOKE, this.getX() + 0.25f - random.nextFloat() * 0.5f, this.getY() + 0.45f - random.nextFloat() * 0.5f, this.getZ() + 0.25f - random.nextFloat() * 0.5f, 0, 0.005, 0);
                }
        }
    }
}
