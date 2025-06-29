package net.luckystudios.entity.custom.cannon_ball.wind_bomb;

import net.luckystudios.blocks.ModBlocks;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.AbstractNewProjectile;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class WindBomb extends AbstractCannonBall {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, true, Optional.of(6F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

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
        this.level()
                .explode(
                        this,
                        null,
                        EXPLOSION_DAMAGE_CALCULATOR,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        3F,
                        false,
                        Level.ExplosionInteraction.TRIGGER,
                        ParticleTypes.GUST_EMITTER_SMALL,
                        ParticleTypes.GUST_EMITTER_LARGE,
                        SoundEvents.WIND_CHARGE_BURST
                );
        this.discard();
    }
}
