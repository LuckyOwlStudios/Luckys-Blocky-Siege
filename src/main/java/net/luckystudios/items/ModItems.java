package net.luckystudios.items;

import net.luckystudios.BlockySiege;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BlockySiege.MOD_ID);

    public static final DeferredItem<Item> GILDED_HORSE_ARMOR = ITEMS.register("gilded_horse_armor",
            () -> new AnimalArmorItem(new Item.Properties()));
    public static final DeferredItem<Item> RAW_BISMUTH = ITEMS.register("raw_bismuth",
            () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
