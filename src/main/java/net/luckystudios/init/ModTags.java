package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static final TagKey<Item> CANNON_AMMO = createItemTag("cannon_ammo");
    public static final TagKey<Item> BOTTLED_AMMO = createItemTag("bottled_ammo");
    public static final TagKey<Item> MULTI_CANNON_SHOOTABLE = createItemTag("multi_cannon_shootable");
    public static final TagKey<Item> SPEWER_AMMO = createItemTag("spewer_ammo");

    public static final TagKey<EntityType<?>> STRONG_PROJECTILE = createEntityTag("strong_projectile");

    private static TagKey<Item> createItemTag(String name) {
        return ItemTags.create(BlockySiege.id(name));
    }

    private static TagKey<Block> createBlockTag(String name) {
        return BlockTags.create(BlockySiege.id(name));
    }

    private static TagKey<EntityType<?>> createEntityTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, BlockySiege.id(name));
    }
}
