package net.luckystudios.entity.custom.cannon_ball.types.explosive_barrel;

import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.AbstractNewProjectile;
import net.luckystudios.entity.custom.explosive_barrel.PrimedExplosiveBarrel;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

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
                this.level().addParticle(ParticleTypes.SMOKE,
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
        return SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR;
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
        PrimedExplosiveBarrel.explode(level(), this.getOwner(), this.position());
        this.discard();
    }
}
