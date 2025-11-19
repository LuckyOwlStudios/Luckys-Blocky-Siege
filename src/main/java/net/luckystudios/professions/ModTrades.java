package net.luckystudios.professions;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.luckystudios.BlockySiege;
import net.luckystudios.init.ModBlocks;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;

@EventBusSubscriber(modid = BlockySiege.MOD_ID)
public class ModTrades {

    @SubscribeEvent
    public static void addTrades(VillagerTradesEvent event) {

        if (event.getType() == ModVillagerProfessions.SIEGE_ENGINEER_PROFESSION.value()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            // Level one trader
            trades.get(1).add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 2),
                    new ItemStack(Items.GUNPOWDER), 12, 10, 0.25f));
            trades.get(1).add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 6),
                    new ItemStack(ModBlocks.EXPLOSIVE_BARREL, 1), 12, 16, 0.25f));
            trades.get(2).add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 12),
                    new ItemStack(ModBlocks.CANNON_BALL.asItem(), 1), 12, 16, 0.25f));
            trades.get(3).add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 16),
                    new ItemStack(ModBlocks.CANNON.asItem(), 1), 12, 16, 0.25f));
            trades.get(4).add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 20),
                    new ItemStack(ModBlocks.MULTI_CANNON.asItem(), 1), 12, 16, 0.25f));
            trades.get(4).add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 12),
                    new ItemStack(ModBlocks.VOLLEY_RACK.asItem(), 1), 12, 16, 0.25f));
            trades.get(5).add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 16),
                    new ItemStack(ModBlocks.SPEWER_CANNON.asItem(), 1), 12, 16, 0.25f));
        }
    }
}
