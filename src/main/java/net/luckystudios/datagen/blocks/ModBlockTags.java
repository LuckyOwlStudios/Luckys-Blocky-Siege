package net.luckystudios.datagen.blocks;

import net.luckystudios.BlockySiege;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {

    // Worldgen tags
    public static final TagKey<Block> OVERWORLD_REPLACEABLE = createTag("overworld_replaceable");

    private static TagKey<Block> createTag(String name) {
        return BlockTags.create(BlockySiege.id(name));
    }
}
