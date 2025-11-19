package net.luckystudios.datagen.types;

import net.luckystudios.BlockySiege;
import net.luckystudios.init.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, BlockySiege.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.CANNON.get())
                .add(ModBlocks.MULTI_CANNON.get())
                .add(ModBlocks.SPEWER_CANNON.get())
                .add(ModBlocks.CANNON_BALL.get())
                .add(ModBlocks.BALLISTA_BLOCK.get())
        ;

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.EXPLOSIVE_BARREL.get())
                .add(ModBlocks.VOLLEY_RACK.get())
        ;

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.CANNON.get())
                .add(ModBlocks.MULTI_CANNON.get())
                .add(ModBlocks.SPEWER_CANNON.get())
                .add(ModBlocks.CANNON_BALL.get())
        ;

        tag(BlockTags.create(ResourceLocation.fromNamespaceAndPath("supplementaries", "lightable_by_gunpowder"))).add(ModBlocks.CANNON.get());

    }
}
