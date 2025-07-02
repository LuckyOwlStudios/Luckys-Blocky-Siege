package net.luckystudios.datagen.items;

import net.luckystudios.BlockySiege;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> CANNON_AMMO = createTag("cannon_ammo");

    public static final TagKey<Item> BOTTLED_AMMO = createTag("bottled_ammo");

    private static TagKey<Item> createTag(String name) {
        return ItemTags.create(BlockySiege.id(name));
    }
}
