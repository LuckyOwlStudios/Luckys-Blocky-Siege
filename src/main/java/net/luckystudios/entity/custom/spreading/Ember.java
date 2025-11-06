package net.luckystudios.entity.custom.spreading;

import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModItems;
import net.luckystudios.init.ModSoundEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Ember extends AbstractSpreadingProjectile {

    public Ember(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public Ember(Level level, double x, double y, double z) {
        super(ModEntityTypes.EMBER.get(), level, x, y, z);
    }

    public Ember(LivingEntity shooter, Level level) {
        super(ModEntityTypes.EMBER.get(), shooter, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.WOODEN_SHRAPNEL.asItem();
    }

    @Override
    protected List<ParticleOptions> getTrailParticles() {
        return List.of(ParticleTypes.FLAME, ParticleTypes.SMOKE);
    }

    @Override
    protected SoundEvent impactSound() {
        return ModSoundEvents.IMPACT_FIERY.get();
    }

    @Override
    protected Block blockToPlace() {
        return ModBlocks.EMBER_PILE.get();
    }
}
