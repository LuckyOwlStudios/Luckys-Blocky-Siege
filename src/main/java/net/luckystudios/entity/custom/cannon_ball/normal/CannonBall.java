package net.luckystudios.entity.custom.cannon_ball.normal;

import net.luckystudios.blocks.ModBlocks;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.AbstractNewProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.Optional;

public class CannonBall extends AbstractCannonBall {

    public CannonBall(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public CannonBall(Level level, double x, double y, double z) {
        super(ModEntityTypes.CANNON_BALL.get(), x, y, z, level, ModBlocks.CANNON_BALL.toStack());
    }

    public CannonBall(Level level, LivingEntity owner) {
        super(ModEntityTypes.CANNON_BALL.get(), owner, level, ModBlocks.CANNON_BALL.toStack());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModBlocks.CANNON_BALL.asItem().getDefaultInstance();
    }

    @Override
    protected List<ParticleOptions> getTrailParticles() {
        ParticleOptions smokeParticle = ParticleTypes.CAMPFIRE_COSY_SMOKE;
        return List.of(smokeParticle);
    }

    @Override
    protected float baseDamage() {
        return 20;
    }

    @Override
    protected SoundEvent impactSound() {
        return SoundEvents.ZOMBIE_ATTACK_IRON_DOOR;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos pos = result.getBlockPos();
        double speed = this.getDeltaMovement().length();
        if (speed < 1) return;

        if (this.level().isClientSide()) return;

        this.level().explode(
                null,
                null,
                new ExplosionDamageCalculator() {

                    @Override
                    public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, FluidState fluid) {
                        if (state.isAir()) return Optional.empty();

                        float blockRes = state.getExplosionResistance(level, pos, explosion);
                        float fluidRes = fluid.getExplosionResistance(level, pos, explosion);

                        float resistance = Math.max(blockRes, fluidRes);

                        // Cut resistance in quarters to improve destructive power
                        return Optional.of(resistance * 0.25F);
                    }

                    @Override
                    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
                        return false;
                    }

                    @Override
                    public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float explosionPower) {
                        return true; // ✅ Allow block damage
                    }

                    @Override
                    public float getKnockbackMultiplier(Entity entity) {
                        return entity instanceof AbstractCannonBall ? 0.0F : 1.0F; // ✅ No knockback for the cannon ball itself
                    }
                },
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                1.5F, // radius
                false, // fire
                Level.ExplosionInteraction.BLOCK,
                false, // spawn particles
                ParticleTypes.EXPLOSION,
                ParticleTypes.EXPLOSION_EMITTER,
                SoundEvents.GENERIC_EXPLODE
        );
    }

    @Override
    public void explode() {

    }
}
