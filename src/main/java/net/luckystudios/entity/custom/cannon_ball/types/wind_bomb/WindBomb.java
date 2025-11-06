package net.luckystudios.entity.custom.cannon_ball.types.wind_bomb;

import net.luckystudios.init.ModBlocks;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.AbstractNewProjectile;
import net.luckystudios.util.ParticleHandler;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class WindBomb extends AbstractCannonBall {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, true, Optional.of(6F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    private static final int CLOUD_PARTICLE_COUNT = 80; // Number of cloud particles
    private static final double CLOUD_SPREAD_RADIUS = 4.0; // How far clouds spread
    private static final double PARTICLE_SPEED_MULTIPLIER = 0.3; // Speed of particle spread

    public WindBomb(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public WindBomb(Level level, double x, double y, double z) {
        super(ModEntityTypes.WIND_BOMB.get(), x, y, z, level, ModBlocks.WIND_BOMB.toStack());
    }

    public WindBomb(Level level, LivingEntity owner) {
        super(ModEntityTypes.WIND_BOMB.get(), owner, level, ModBlocks.WIND_BOMB.toStack());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModBlocks.WIND_BOMB.asItem().getDefaultInstance();
    }

    @Override
    protected List<ParticleOptions> getTrailParticles() {
        ParticleOptions whiteAsh = ParticleTypes.WHITE_ASH;
        return List.of(whiteAsh);
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
    public void explode() {
        double explosionX = this.getX();
        double explosionY = this.getY();
        double explosionZ = this.getZ();

        // Original wind charge explosion
        this.level()
                .explode(
                        this,
                        null,
                        EXPLOSION_DAMAGE_CALCULATOR,
                        explosionX,
                        explosionY,
                        explosionZ,
                        3F,
                        false,
                        Level.ExplosionInteraction.TRIGGER,
                        ParticleTypes.GUST_EMITTER_SMALL,
                        ParticleTypes.GUST_EMITTER_LARGE,
                        SoundEvents.WIND_CHARGE_BURST
                );

        // Enhanced cloud particle explosion

        if (level() instanceof ServerLevel serverLevel) {
            ParticleHandler.createWindBombExplosion(this.level(), explosionX, explosionY, explosionZ, this.random);
        }

        // Play enhanced sound effects
        this.level().playSound(null, explosionX, explosionY, explosionZ, SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.BLOCKS, 2.0F, 0.25F);
        this.level().playSound(null, explosionX, explosionY, explosionZ, SoundEvents.PLAYER_BREATH, SoundSource.BLOCKS, 1.5F, 0.5F);

        this.discard();
    }
}