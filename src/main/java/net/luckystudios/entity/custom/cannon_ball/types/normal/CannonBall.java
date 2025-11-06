package net.luckystudios.entity.custom.cannon_ball.types.normal;

import net.luckystudios.init.ModBlocks;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class CannonBall extends AbstractCannonBall {

    private static final float MIN_PENETRATION_SPEED = 0.8F;
    private static final float BASE_SPEED_LOSS = 0.1F;
    private static final int MAX_PENETRATION_BLOCKS = 8;
    private static final float INITIAL_PENETRATION_POWER = 15.0F; // Starting penetration power
    private int blocksPenetrated = 0;
    private float penetrationPower = INITIAL_PENETRATION_POWER;

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
        return List.of(ParticleTypes.SMOKE, ParticleTypes.FLAME);
    }

    @Override
    protected float baseDamage() {
        return 25; // Increased base damage
    }

    @Override
    protected SoundEvent impactSound() {
        return SoundEvents.ANVIL_LAND;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockPos hitPos = result.getBlockPos();
        BlockState hitState = this.level().getBlockState(hitPos);
        double currentSpeed = this.getDeltaMovement().length();

        if (this.level().isClientSide()) {
            // Client-side particles and effects
            spawnImpactParticles(hitPos);
            return;
        }

        // Check if we can penetrate this block
        if (canPenetrateBlock(hitState, currentSpeed)) {
            penetrateBlock(hitPos, hitState, result);
        } else {
            // Create explosion and stop the projectile
            createExplosion(hitPos, currentSpeed);
            super.onHitBlock(result);
        }
    }

    private boolean canPenetrateBlock(BlockState state, double speed) {
        // Can't penetrate if too slow or hit too many blocks
        if (speed < MIN_PENETRATION_SPEED || blocksPenetrated >= MAX_PENETRATION_BLOCKS) {
            return false;
        }

        // Can't penetrate bedrock, obsidian, or other super-hard blocks
        if (state.is(Blocks.BEDROCK) || state.is(Blocks.OBSIDIAN) ||
                state.is(Blocks.CRYING_OBSIDIAN) || state.is(Blocks.RESPAWN_ANCHOR)) {
            return false;
        }

        // Check if we have enough penetration power for this block
        float blockHardness = state.getDestroySpeed(this.level(), null);
        if (blockHardness < 0) return false; // Unbreakable blocks

        // Calculate penetration cost based on block hardness
        float penetrationCost = calculatePenetrationCost(blockHardness);

        // Can only penetrate if we have enough power remaining
        return penetrationPower >= penetrationCost;
    }

    private void penetrateBlock(BlockPos pos, BlockState state, BlockHitResult result) {
        // Calculate penetration costs
        float blockHardness = Math.max(0.1F, state.getDestroySpeed(this.level(), pos));
        float penetrationCost = calculatePenetrationCost(blockHardness);
        float speedReduction = calculateSpeedReduction(blockHardness);

        // Break the block
        this.level().destroyBlock(pos, true);

        // Reduce penetration power and speed
        penetrationPower -= penetrationCost;
        penetrationPower = Math.max(0, penetrationPower); // Don't go negative

        Vec3 currentMotion = this.getDeltaMovement();
        Vec3 newMotion = currentMotion.scale(1.0 - speedReduction);
        this.setDeltaMovement(newMotion);

        blocksPenetrated++;

        // Create smaller explosion for penetration (size based on remaining power)
        createPenetrationExplosion(pos, currentMotion.length());

        // Play sound with pitch based on block hardness
        float pitch = Math.max(0.5F, 1.2F - (blockHardness * 0.1F));
        this.level().playSound(null, pos, SoundEvents.STONE_BREAK,
                this.getSoundSource(), 1.0F, pitch);

        // Debug info (remove in production)
        // System.out.println("Penetrated block with hardness " + blockHardness +
        //                   ". Remaining penetration power: " + penetrationPower);
    }

    private float calculatePenetrationCost(float blockHardness) {
        // Cost scales exponentially with hardness
        // Soft blocks (dirt, sand) cost ~0.5-1.0 power
        // Medium blocks (stone, wood) cost ~2.0-4.0 power
        // Hard blocks (iron, diamond ore) cost ~6.0-12.0 power
        return 0.5F + (blockHardness * blockHardness * 0.8F);
    }

    private float calculateSpeedReduction(float blockHardness) {
        // Speed reduction is less severe but still scales with hardness
        // Base reduction + hardness modifier
        return BASE_SPEED_LOSS + (blockHardness * 0.02F);
    }

    private void createExplosion(BlockPos pos, double speed) {
        // Scale explosion size based on speed (1.5 to 3.0 radius)
        float explosionRadius = Math.min(3.0F, 1.5F + (float)(speed * 0.3F));

        this.level().explode(
                this, // Set the cannon ball as the source
                null,
                new EnhancedExplosionDamageCalculator(),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                explosionRadius,
                false, // no fire
                Level.ExplosionInteraction.BLOCK,
                false, // no particles (we'll handle our own)
                ParticleTypes.EXPLOSION,
                ParticleTypes.EXPLOSION_EMITTER,
                SoundEvents.GENERIC_EXPLODE
        );
    }

    private void createPenetrationExplosion(BlockPos pos, double speed) {
        // Explosion size based on remaining penetration power and speed
        float basePower = Math.min(1.8F, 0.6F + (float)(speed * 0.15F));
        float powerMultiplier = Math.min(1.5F, penetrationPower / INITIAL_PENETRATION_POWER);
        float explosionRadius = basePower * powerMultiplier;

        this.level().explode(
                this,
                null,
                new PenetrationExplosionDamageCalculator(),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                explosionRadius,
                false,
                Level.ExplosionInteraction.BLOCK,
                false,
                ParticleTypes.SMOKE,
                ParticleTypes.FLAME,
                SoundEvents.GENERIC_EXPLODE
        );
    }

    private void spawnImpactParticles(BlockPos pos) {
        // Spawn custom impact particles on client side
        for (int i = 0; i < 10; i++) {
            this.level().addParticle(ParticleTypes.EXPLOSION,
                    pos.getX() + 0.5 + (this.random.nextGaussian() * 0.3),
                    pos.getY() + 0.5 + (this.random.nextGaussian() * 0.3),
                    pos.getZ() + 0.5 + (this.random.nextGaussian() * 0.3),
                    this.random.nextGaussian() * 0.1,
                    this.random.nextGaussian() * 0.1,
                    this.random.nextGaussian() * 0.1);
        }
    }

    @Override
    public void explode() {
        // Called when manually triggered
        if (!this.level().isClientSide()) {
            createExplosion(this.blockPosition(), this.getDeltaMovement().length());
            this.discard();
        }
    }

    // Enhanced explosion damage calculator for final impact
    private static class EnhancedExplosionDamageCalculator extends ExplosionDamageCalculator {
        @Override
        public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, FluidState fluid) {
            if (state.isAir()) return Optional.empty();

            float blockRes = state.getExplosionResistance(level, pos, explosion);
            float fluidRes = fluid.getExplosionResistance(level, pos, explosion);
            float resistance = Math.max(blockRes, fluidRes);

            // Significantly reduce resistance for better destruction
            return Optional.of(resistance * 0.15F);
        }

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            // Damage all entities except the cannon ball itself
            return !(entity instanceof CannonBall);
        }

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float explosionPower) {
            return !state.is(Blocks.BEDROCK); // Everything except bedrock can be destroyed
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            if (entity instanceof AbstractCannonBall) return 0.0F;
            if (entity instanceof LivingEntity) return 2.0F; // Increased knockback for living entities
            return 1.5F;
        }
    }

    // Lighter explosion for penetration
    private static class PenetrationExplosionDamageCalculator extends ExplosionDamageCalculator {
        @Override
        public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, FluidState fluid) {
            if (state.isAir()) return Optional.empty();

            float blockRes = state.getExplosionResistance(level, pos, explosion);
            float fluidRes = fluid.getExplosionResistance(level, pos, explosion);
            float resistance = Math.max(blockRes, fluidRes);

            return Optional.of(resistance * 0.3F); // Less destructive than final explosion
        }

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return !(entity instanceof CannonBall) && entity instanceof LivingEntity; // Only damage living entities
        }

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float explosionPower) {
            // Penetration explosions are more selective and weaker as power decreases
            float hardness = state.getDestroySpeed(level, pos);
            CannonBall cannonBall = (CannonBall) explosion.getDirectSourceEntity();

            if (cannonBall != null) {
                // Only destroy blocks we can still penetrate based on remaining power
                float penetrationCost = cannonBall.calculatePenetrationCost(hardness);
                boolean canDestroy = hardness >= 0 && hardness <= 4.0F &&
                        !state.is(Blocks.BEDROCK) &&
                        cannonBall.penetrationPower > (penetrationCost * 0.5F);
                return canDestroy;
            }

            return hardness >= 0 && hardness <= 3.0F && !state.is(Blocks.BEDROCK);
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            if (entity instanceof AbstractCannonBall) return 0.0F;
            return 0.8F; // Reduced knockback for penetration explosions
        }
    }
}