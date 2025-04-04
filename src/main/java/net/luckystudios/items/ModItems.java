package net.luckystudios.items;

import net.luckystudios.BlockySiege;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BlockySiege.MOD_ID);

//    public static final DeferredItem<Item> SAMPLE = ITEMS.register("sample",
//            () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
