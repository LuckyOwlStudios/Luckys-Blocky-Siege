package net.luckystudios.professions;

import net.luckystudios.init.ModBlocks;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.function.BiConsumer;

/**
 * The siege engineer's trades. Each loader feeds these into its own villager trade hook.
 */
public class ModTrades {

    /**
     * @param trades receives (villager level, trade) for every trade of the siege engineer
     */
    public static void addSiegeEngineerTrades(BiConsumer<Integer, VillagerTrades.ItemListing> trades) {
        // Level one trader
        trades.accept(1, (entity, randomSource) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 2),
                new ItemStack(Items.GUNPOWDER), 12, 10, 0.25f));
        trades.accept(1, (entity, randomSource) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 6),
                new ItemStack(ModBlocks.EXPLOSIVE_BARREL.get(), 1), 12, 16, 0.25f));
        trades.accept(2, (entity, randomSource) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 12),
                new ItemStack(ModBlocks.CANNONBALL.get().asItem(), 1), 12, 16, 0.25f));
        trades.accept(3, (entity, randomSource) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 16),
                new ItemStack(ModBlocks.CANNON.get().asItem(), 1), 12, 16, 0.25f));
        trades.accept(4, (entity, randomSource) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 20),
                new ItemStack(ModBlocks.MULTI_CANNON.get().asItem(), 1), 12, 16, 0.25f));
        trades.accept(4, (entity, randomSource) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 12),
                new ItemStack(ModBlocks.VOLLEY_RACK.get().asItem(), 1), 12, 16, 0.25f));
        trades.accept(5, (entity, randomSource) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 16),
                new ItemStack(ModBlocks.SPEWER_CANNON.get().asItem(), 1), 12, 16, 0.25f));
    }
}
