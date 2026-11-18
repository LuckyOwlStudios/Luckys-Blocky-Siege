package net.luckystudios.datagen.types;

import net.luckystudios.init.ModBlocks;
import net.luckystudios.blocks.custom.cannon_ammo.AbstractCannonBallBlock;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.luckystudios.BlockySiege;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.IntStream;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // Needs to add ModBlocks.CANNON_BARREL.get() to the loot table
        dropSelf(ModBlocks.CANNON.get());
        dropSelf(ModBlocks.MULTI_CANNON.get());
        dropSelf(ModBlocks.SPEWER_CANNON.get());
        dropSelf(ModBlocks.VOLLEY_RACK.get());
        this.add(ModBlocks.EMBER_PILE.get(), noDrop());
        this.add(ModBlocks.FROST_PILE.get(), noDrop());
        this.add(ModBlocks.CANNONBALL.get(), createCannonAmmoStack(ModBlocks.CANNONBALL.get()));
        this.add(ModBlocks.FIRE_BOMB.get(), createCannonAmmoStack(ModBlocks.FIRE_BOMB.get()));
        this.add(ModBlocks.FROST_BOMB.get(), createCannonAmmoStack(ModBlocks.FROST_BOMB.get()));
        this.add(ModBlocks.WIND_BOMB.get(), createCannonAmmoStack(ModBlocks.WIND_BOMB.get()));
        dropSelf(ModBlocks.EXPLOSIVE_BARREL.get());

        dropSelf(ModBlocks.BALLISTA_BLOCK.get());
    }

    protected LootTable.Builder createCannonAmmoStack(Block ammoStack) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(
                                        this.applyExplosionDecay(
                                                ammoStack,
                                                LootItem.lootTableItem(ammoStack)
                                                        .apply(
                                                                IntStream.rangeClosed(1, 4).boxed().toList(),
                                                                intValue -> SetItemCountFunction.setCount(ConstantValue.exactly((float)intValue.intValue()))
                                                                        .when(
                                                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(ammoStack)
                                                                                        .setProperties(
                                                                                                StatePropertiesPredicate.Builder.properties().hasProperty(AbstractCannonBallBlock.STACK, intValue.intValue())
                                                                                        )
                                                                        )
                                                        )
                                        )
                                )
                );
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(BlockySiege.MOD_ID))::iterator;
    }
}
