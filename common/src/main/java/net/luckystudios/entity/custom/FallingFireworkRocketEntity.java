package net.luckystudios.entity.custom;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FallingFireworkRocketEntity extends FireworkRocketEntity {

    public FallingFireworkRocketEntity(EntityType<? extends FireworkRocketEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FallingFireworkRocketEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
    }

    public FallingFireworkRocketEntity(Level level, @Nullable Entity shooter, double x, double y, double z, ItemStack stack) {
        super(level, shooter, x, y, z, stack);
    }

    public FallingFireworkRocketEntity(Level level, ItemStack stack, LivingEntity shooter) {
        super(level, stack, shooter);
    }

    public FallingFireworkRocketEntity(Level level, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
        super(level, stack, x, y, z, shotAtAngle);
        this.setNoGravity(false);
    }

    public FallingFireworkRocketEntity(Level level, ItemStack stack, Entity shooter, double x, double y, double z, boolean shotAtAngle) {
        super(level, stack, shooter, x, y, z, shotAtAngle);
    }

    @Override
    public void tick() {
        super.tick();
        this.applyGravity();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.1F;
    }
}
