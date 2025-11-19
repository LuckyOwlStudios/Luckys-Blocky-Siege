package net.luckystudios.entity.custom.new_boats;

import net.luckystudios.init.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;

public class SloopEntity extends Boat {
    public SloopEntity(EntityType<? extends Boat> entityType, Level level) {
        super(ModEntityTypes.SLOOP.get(), level);
    }

    public SloopEntity(Level level, double x, double y, double z) {
        super(level, x, y, z);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0F)
                .add(Attributes.ARMOR, 4.0F);
    }
}
