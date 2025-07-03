package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static final TagKey<Item> CANNON_AMMO = createItemTag("cannon_ammo");
    public static final TagKey<Item> BOTTLED_AMMO = createItemTag("bottled_ammo");

    private static TagKey<Item> createItemTag(String name) {
        return ItemTags.create(BlockySiege.id(name));
    }

    private static TagKey<Block> createBlockTag(String name) {
        return BlockTags.create(BlockySiege.id(name));
    }
}
