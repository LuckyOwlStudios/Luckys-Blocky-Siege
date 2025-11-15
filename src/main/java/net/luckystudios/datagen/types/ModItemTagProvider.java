package net.luckystudios.datagen.types;

import net.luckystudios.BlockySiege;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
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
        tag(ModTags.CANNON_AMMO)
                .add(ModBlocks.CANNON_BALL.get().asItem())
                .add(ModBlocks.EXPLOSIVE_BARREL.get().asItem())
                .add(ModBlocks.FIRE_BOMB.get().asItem())
                .add(ModBlocks.FROST_BOMB.get().asItem())
                .add(ModBlocks.WIND_BOMB.get().asItem())
                .addOptional(ResourceLocation.fromNamespaceAndPath("amendments", "dragon_charge"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("supplementaries", "bomb"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("supplementaries", "bomb_blue"))
        ;

        tag(ModTags.BOTTLED_AMMO)
                .add(ModBlocks.FIRE_BOMB.get().asItem())
                .add(ModBlocks.FROST_BOMB.get().asItem())
                .add(ModBlocks.WIND_BOMB.get().asItem())
        ;

        tag(ModTags.MULTI_CANNON_SHOOTABLE)
                .add(Items.EGG, Items.SNOWBALL, Items.FIRE_CHARGE, Items.FIREWORK_STAR, Items.WIND_CHARGE)
        ;

        tag(ModTags.SPEWER_AMMO)
                .addTag(Tags.Items.BUCKETS_LAVA)
                .addTag(Tags.Items.BUCKETS_WATER)
                .addTag(Tags.Items.BUCKETS_POWDER_SNOW)
                .addTag(Tags.Items.POTIONS)
        ;
    }
}
