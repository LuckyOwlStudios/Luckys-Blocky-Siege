package net.luckystudios.datagen.types;

import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModItems;
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

        String HAS_IRON = "has_iron";

        // Cannon Stuff
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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.EXPLOSIVE_KEG.get())
                .pattern(" # ")
                .pattern("#@#")
                .pattern(" # ")
                .define('@', Items.GUNPOWDER)
                .define('#', ItemTags.PLANKS)
                .unlockedBy(HAS_IRON, has(Items.GUNPOWDER))
                .save(recipeOutput);
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
