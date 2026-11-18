package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.Item;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public class ModItems {
    public static final RegistryObject<Item> WOODEN_SHRAPNEL = register("wooden_shrapnel",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FORT_BANNER_PATTERN = register("fort_banner_pattern",
            () -> new BannerPatternItem(ModTags.PATTERN_ITEM_FORT, new Item.Properties()));

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> factory) {
        return Services.REGISTRY.register(BuiltInRegistries.ITEM, name, factory);
    }

    public static void init() {
    }
}
