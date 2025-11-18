package net.luckystudios.entity.custom.liquid_projectile;

import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModItems;
import net.luckystudios.init.ModSoundEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SnowProjectile extends AbstractLiquidProjectile {

    public SnowProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SnowProjectile(Level level, double x, double y, double z) {
        super(ModEntityTypes.ICE_SHARD.get(), level, x, y, z);
    }

    public SnowProjectile(LivingEntity shooter, Level level) {
        super(ModEntityTypes.ICE_SHARD.get(), shooter, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.WOODEN_SHRAPNEL.asItem();
    }

    @Override
    protected List<ParticleOptions> getTrailParticles() {
        return List.of(ParticleTypes.CLOUD, ParticleTypes.SNOWFLAKE);
    }

    @Override
    protected SoundEvent impactSound() {
        return ModSoundEvents.IMPACT_ICY.get();
    }

    @Override
    protected BlockState blockStateToPlace() {
        return ModBlocks.FROST_PILE.get().defaultBlockState();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().getBlockState(blockPosition()) == Blocks.WATER.defaultBlockState()) {
            level().setBlock(blockPosition(), Blocks.FROSTED_ICE.defaultBlockState(), 3);
        }
    }
}
