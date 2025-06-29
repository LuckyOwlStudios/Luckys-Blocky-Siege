package net.luckystudios.datagen.items;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, BlockySiege.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModItemTags.CANNON_AMMO)
                .add(ModBlocks.CANNON_BALL.get().asItem())
                .add(ModBlocks.EXPLOSIVE_KEG.get().asItem())
                .add(ModBlocks.FIRE_BOMB.get().asItem())
                .add(ModBlocks.FROST_BOMB.get().asItem())
                .add(ModBlocks.WIND_BOMB.get().asItem())
        ;
    }
}
