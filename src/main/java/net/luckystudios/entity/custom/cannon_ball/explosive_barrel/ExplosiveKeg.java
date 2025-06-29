package net.luckystudios.entity.custom.cannon_ball.explosive_barrel;

import net.luckystudios.blocks.ModBlocks;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.AbstractNewProjectile;
import net.luckystudios.entity.custom.explosive_barrel.PrimedExplosiveBarrel;
import net.luckystudios.sounds.ModSoundEvents;
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

public class ExplosiveKeg extends AbstractCannonBall {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, true, Optional.of(.25F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    public ExplosiveKeg(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ExplosiveKeg(Level level, double x, double y, double z) {
        super(ModEntityTypes.EXPLOSIVE_KEG.get(), x, y, z, level, ModBlocks.EXPLOSIVE_KEG.toStack());
    }

    public ExplosiveKeg(Level level, LivingEntity owner) {
        super(ModEntityTypes.EXPLOSIVE_KEG.get(), owner, level, ModBlocks.EXPLOSIVE_KEG.toStack());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModBlocks.EXPLOSIVE_KEG.asItem().getDefaultInstance();
    }

    @Override
    protected List<ParticleOptions> getTrailParticles() {
        ParticleOptions smokeParticle = ParticleTypes.SMOKE;
        return List.of(smokeParticle);
    }

    @Override
    protected float baseDamage() {
        return 8;
    }

    @Override
    protected SoundEvent impactSound() {
        return SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR;
    }

    @Override
    public void explode() {
        PrimedExplosiveBarrel.explode(level(), this.getOwner(), this.position());
        this.discard();
    }
}
