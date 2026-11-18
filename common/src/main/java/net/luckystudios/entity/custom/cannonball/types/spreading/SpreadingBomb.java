package net.luckystudios.entity.custom.cannonball.types.spreading;

import net.luckystudios.entity.custom.cannonball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannonball.AbstractNewProjectile;
import net.luckystudios.entity.custom.liquid_projectile.AbstractLiquidProjectile;
import net.luckystudios.util.ParticleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
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

public abstract class SpreadingBomb extends AbstractCannonBall {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, true, Optional.of(.25F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    private Vec3 hitDirection = null; // Store the direction for spawning projectiles

    public SpreadingBomb(EntityType<? extends AbstractNewProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SpreadingBomb(EntityType<? extends AbstractNewProjectile> entityType, Level level, double x, double y, double z, ItemStack itemStack) {
        super(entityType, x, y, z, level, itemStack);
    }

    public SpreadingBomb(EntityType<? extends AbstractNewProjectile> entityType, Level level, LivingEntity owner, ItemStack itemStack) {
        super(entityType, owner, level, itemStack);
    }

    public abstract Projectile spreadingProjectile();
    public abstract Block blockToSpread();
    public abstract ParticleOptions particle();

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // Direction is the normal of the block face hit
        Direction blockFace = result.getDirection();
        hitDirection = Vec3.atLowerCornerOf(blockFace.getNormal());
        explode();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Direction is opposite to the projectile's movement
        Vec3 movement = getDeltaMovement();
        if (movement.lengthSqr() > 0) {
            hitDirection = movement.normalize().reverse();
        } else {
            // Fallback if no movement (shouldn't happen, but just in case)
            hitDirection = new Vec3(0, 1, 0);
        }
        explode();
    }

    public void explode() {
        if (level() instanceof ServerLevel serverLevel) {
            ParticleHandler.spawnParticleSphere(serverLevel, particle(), getX(), getY(), getZ(), 40, 0.5);
        }

        // Place blocks in a 3-block radius sphere around impact
        placeBlocksInSphere();

        // Spawn spreading projectiles based on hit direction
        for (int index0 = 0; index0 < 6; index0++) {
            Projectile projectile = spreadingProjectile();

            // Calculate spread direction based on hit direction
            Vec3 spreadDirection = calculateSpreadDirection(index0);

            // Set position at impact location
            projectile.setPos(getX(), getY(), getZ());

            // Shoot in the calculated direction
            projectile.shoot(spreadDirection.x, spreadDirection.y, spreadDirection.z, 0.25F, 24);
            level().addFreshEntity(projectile);
        }
    }

    private void placeBlocksInSphere() {
        BlockPos centerPos = blockPosition();
        int radius = 3;

        // Iterate through all positions in a cube around the center
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius; y <= radius; y++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);

                    // Calculate distance from center to check if it's within sphere
                    double distance = Math.sqrt(x * x + y * y + z * z);

                    // Only place blocks within the radius
                    if (distance <= radius) {
                        // Check if the position can be replaced (not solid blocks, liquids are ok to replace)
                        if (level().getBlockState(checkPos).canBeReplaced()) {
                            // Check if there's a solid surface below (debris needs support)
                            BlockPos belowPos = checkPos.below();
                            if (!level().getBlockState(belowPos).canBeReplaced() &&
                                    !level().getBlockState(belowPos).is(blockToSpread())) {

                                // Random chance to place blocks for a more natural look (80% chance)
                                if (level().getRandom().nextFloat() < 0.8f) {
                                    level().setBlock(checkPos, blockToSpread().defaultBlockState(), 3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Vec3 calculateSpreadDirection(int index) {
        if (hitDirection == null) {
            hitDirection = new Vec3(0, 1, 0); // Default upward direction
        }

        // Create a spread pattern around the hit direction
        // We'll create 6 directions in a cone around the hit direction

        // Calculate angles for spreading in a cone pattern
        float angleStep = 360f / 6f; // 60 degrees between each projectile
        float currentAngle = angleStep * index;
        float spreadAngle = 30f; // Cone spread angle (adjust as needed)

        // Convert to radians
        float angleRad = (float) Math.toRadians(currentAngle);
        float spreadRad = (float) Math.toRadians(spreadAngle);

        // Create perpendicular vectors to the hit direction for spreading
        Vec3 perpendicular1, perpendicular2;

        // Find two perpendicular vectors to hitDirection
        if (Math.abs(hitDirection.y) < 0.9) {
            // hitDirection is not mostly vertical
            perpendicular1 = new Vec3(0, 1, 0).cross(hitDirection).normalize();
        } else {
            // hitDirection is mostly vertical, use x-axis
            perpendicular1 = new Vec3(1, 0, 0).cross(hitDirection).normalize();
        }
        perpendicular2 = hitDirection.cross(perpendicular1).normalize();

        // Calculate spread offset
        Vec3 spreadOffset = perpendicular1.scale(Math.cos(angleRad) * Math.sin(spreadRad))
                .add(perpendicular2.scale(Math.sin(angleRad) * Math.sin(spreadRad)));

        // Combine hit direction with spread offset
        Vec3 finalDirection = hitDirection.scale(Math.cos(spreadRad)).add(spreadOffset);

        return finalDirection.normalize();
    }
}