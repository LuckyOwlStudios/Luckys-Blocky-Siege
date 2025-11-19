package net.luckystudios.datagen.types;

import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        String HAS_IRON = "has_iron";

        // Cannon Stuff
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CANNON.get())
                .pattern("Z@Z")
                .pattern("@X@")
                .pattern("###")
                .define('@', Items.IRON_BLOCK)
                .define('Z', Items.IRON_INGOT)
                .define('X', Tags.Items.TOOLS_IGNITER)
                .define('#', ItemTags.PLANKS)
                .unlockedBy(HAS_IRON, has(Items.IRON_INGOT))
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(recipeOutput)
        ;
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SPEWER_CANNON.get())
                .pattern("@@@")
                .pattern("@X@")
                .pattern("###")
                .define('@', Items.IRON_INGOT)
                .define('X', Items.CAULDRON)
                .define('#', ItemTags.PLANKS)
                .unlockedBy(HAS_IRON, has(Items.IRON_INGOT))
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(recipeOutput)
        ;
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VOLLEY_RACK.get())
                .pattern("@@@")
                .pattern("@X@")
                .pattern("###")
                .define('@', ItemTags.PLANKS)
                .define('X', Tags.Items.TOOLS_IGNITER)
                .define('#', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_igniter", has(Tags.Items.TOOLS_IGNITER))
                .save(recipeOutput)
        ;
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CANNON_BALL.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Items.IRON_BLOCK)
                .define('#', Items.IRON_INGOT)
                .unlockedBy(HAS_IRON, has(Items.IRON_INGOT))
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(recipeOutput)
        ;
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FIRE_BOMB.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Items.MAGMA_BLOCK)
                .define('#', Blocks.TINTED_GLASS)
                .unlockedBy(HAS_IRON, has(Items.MAGMA_BLOCK))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FROST_BOMB.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Blocks.BLUE_ICE)
                .define('#', Blocks.GLASS)
                .unlockedBy("has_blue_ice", has(Items.BLUE_ICE))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.WIND_BOMB.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Items.WIND_CHARGE)
                .define('#', Blocks.GLASS)
                .unlockedBy("has_wind_charge", has(Items.WIND_CHARGE))
                .save(recipeOutput);
    }
}
