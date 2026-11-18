package net.luckystudios.entity.custom.new_boats;

import net.luckystudios.init.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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

    @Override
    public Item getDropItem() {
        Item item;
        switch (this.getVariant().ordinal()) {
            case 0 -> item = Items.OAK_BOAT;
            case 1 -> item = Items.SPRUCE_BOAT;
            case 2 -> item = Items.BIRCH_BOAT;
            case 3 -> item = Items.JUNGLE_BOAT;
            case 4 -> item = Items.ACACIA_BOAT;
            case 5 -> item = Items.CHERRY_BOAT;
            case 6 -> item = Items.DARK_OAK_BOAT;
            case 7 -> item = Items.MANGROVE_BOAT;
            case 8 -> item = Items.BAMBOO_RAFT;
        }
        return super.getDropItem();
    }
}
