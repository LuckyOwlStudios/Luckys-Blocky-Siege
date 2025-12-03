package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BlockySiege.MOD_ID);

    public static final DeferredItem<Item> WOODEN_SHRAPNEL = ITEMS.register("wooden_shrapnel",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> FORT_BANNER_PATTERN = ITEMS.register("fort_banner_pattern",
            () -> new BannerPatternItem(ModTags.PATTERN_ITEM_FORT, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
