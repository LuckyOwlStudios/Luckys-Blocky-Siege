package net.luckystudios.entity.custom.cannon_ball.fire_bomb;

import net.luckystudios.init.ModBlocks;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.AbstractNewProjectile;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class FireBomb extends AbstractCannonBall {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, true, Optional.of(.25F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    public FireBomb(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FireBomb(Level level, double x, double y, double z) {
        super(ModEntityTypes.FIRE_BOMB.get(), x, y, z, level, ModBlocks.FIRE_BOMB.toStack());
    }

    public FireBomb(Level level, LivingEntity owner) {
        super(ModEntityTypes.FIRE_BOMB.get(), owner, level, ModBlocks.FIRE_BOMB.toStack());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModBlocks.FIRE_BOMB.asItem().getDefaultInstance();
    }

    @Override
    protected List<ParticleOptions> getTrailParticles() {
        ParticleOptions smokeParticle = ParticleTypes.CAMPFIRE_COSY_SMOKE;
        ParticleOptions fireParticle = ParticleTypes.FLAME;
        return List.of(smokeParticle, fireParticle);
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
                        2F,
                        true,
                        Level.ExplosionInteraction.TRIGGER,
                        ParticleTypes.LAVA,
                        ParticleTypes.FLAME,
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSoundEvents.IMPACT_FIERY.get())
                );
        this.discard();
    }
}
