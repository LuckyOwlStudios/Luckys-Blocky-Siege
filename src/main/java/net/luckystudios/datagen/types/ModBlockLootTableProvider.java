package net.luckystudios.datagen.types;

import net.luckystudios.blocks.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // Needs to add feature where they drop depending on how many there are
        dropSelf(ModBlocks.CANNON_BALL.get());
        dropSelf(ModBlocks.KEG_OF_GUNPOWDER.get());
        dropSelf(ModBlocks.BLUNDERBOMB.get());
        dropSelf(ModBlocks.FROST_BOMB.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
