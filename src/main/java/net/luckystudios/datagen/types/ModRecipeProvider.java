package net.luckystudios.datagen.types;

import net.luckystudios.blocks.ModBlocks;
import net.luckystudios.items.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        // Cannon Stuff
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CANNON_BALL.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Items.IRON_BLOCK)
                .define('#', Items.IRON_INGOT)
                .unlockedBy("has_iron", has(Items.IRON_BLOCK)).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.EXPLOSIVE_KEG.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Items.GUNPOWDER)
                .define('#', ItemTags.PLANKS)
                .unlockedBy("has_iron", has(Items.GUNPOWDER)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FIRE_BOMB.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Items.MAGMA_BLOCK)
                .define('#', Blocks.TINTED_GLASS)
                .unlockedBy("has_iron", has(Items.MAGMA_BLOCK)).save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FROST_BOMB.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Blocks.BLUE_ICE)
                .define('#', Blocks.GLASS)
                .unlockedBy("has_blue_ice", has(Items.BLUE_ICE)).save(recipeOutput);

        // Multi-Cannon Stuff
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BULLET.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Items.GUNPOWDER)
                .define('#', Items.IRON_INGOT)
                .unlockedBy("has_iron", has(Items.GUNPOWDER)).save(recipeOutput);
    }
}
