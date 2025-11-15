package net.luckystudios.entity.custom.spreading;

import net.luckystudios.init.*;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
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
        return List.of(ModParticleTypes.FLAME_TRAIL.get());
    }

    @Override
    protected SoundEvent impactSound() {
        return SoundEvents.BLAZE_SHOOT;
    }

    @Override
    public void tick() {
        super.tick();
        if (isInWater()) {
            playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 1.0F);
            if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        this.getX(), this.getY() + 1, this.getZ(),
                        10,  // particle count
                        0.25, 0.25, 0.25,  // spread (x, y, z)
                        0.05  // speed
                );
            }
            this.discard();
        }
    }

    @Override
    protected Block blockToPlace() {
        return Blocks.FIRE;
    }
}
