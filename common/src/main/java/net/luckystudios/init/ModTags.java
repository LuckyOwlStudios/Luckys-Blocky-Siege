package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;

public class ModTags {

    public static final TagKey<Item> CANNON_AMMO = createItemTag("cannon_ammo");
    public static final TagKey<Item> BOTTLED_AMMO = createItemTag("bottled_ammo");
    public static final TagKey<Item> MULTI_CANNON_SHOOTABLE = createItemTag("multi_cannon_shootable");
    public static final TagKey<Item> SPEWER_AMMO = createItemTag("spewer_ammo");

    // Convention ("c") tags shared by NeoForge and Fabric
    public static final TagKey<Item> GUNPOWDERS = createCommonItemTag("gunpowders");
    public static final TagKey<Item> BUCKETS_WATER = createCommonItemTag("buckets/water");
    public static final TagKey<Item> BUCKETS_LAVA = createCommonItemTag("buckets/lava");
    public static final TagKey<Item> BUCKETS_POWDER_SNOW = createCommonItemTag("buckets/powder_snow");

    public static final TagKey<EntityType<?>> STRONG_PROJECTILE = createEntityTag("strong_projectile");
    /** Mobs that man siege weapons on their own. */
    public static final TagKey<EntityType<?>> SIEGE_CREW = createEntityTag("siege_crew");

    public static final TagKey<BannerPattern> PATTERN_ITEM_FORT = createBannerTag("pattern_item/fort");

    private static TagKey<BannerPattern> createBannerTag(String name) {
        return TagKey.create(Registries.BANNER_PATTERN, BlockySiege.id(name));
    }

    private static TagKey<Item> createItemTag(String name) {
        return TagKey.create(Registries.ITEM, BlockySiege.id(name));
    }

    private static TagKey<Item> createCommonItemTag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
    }

    private static TagKey<Block> createBlockTag(String name) {
        return TagKey.create(Registries.BLOCK, BlockySiege.id(name));
    }

    private static TagKey<EntityType<?>> createEntityTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, BlockySiege.id(name));
    }
}
