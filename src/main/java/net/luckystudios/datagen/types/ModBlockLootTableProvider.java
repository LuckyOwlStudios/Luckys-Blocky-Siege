package net.luckystudios.datagen.types;

import net.luckystudios.blocks.ModBlocks;
import net.luckystudios.blocks.custom.cannon_ammo.AbstractCannonBallBlock;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
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
        dropSelf(ModBlocks.IRON_GATE.get());
        this.add(ModBlocks.CANNON_BALL.get(), createCannonAmmoStack(ModBlocks.CANNON_BALL.get()));
        this.add(ModBlocks.EXPLOSIVE_KEG.get(), createCannonAmmoStack(ModBlocks.EXPLOSIVE_KEG.get()));
        this.add(ModBlocks.FIRE_BOMB.get(), createCannonAmmoStack(ModBlocks.FIRE_BOMB.get()));
        this.add(ModBlocks.FROST_BOMB.get(), createCannonAmmoStack(ModBlocks.FROST_BOMB.get()));
        this.add(ModBlocks.WIND_BOMB.get(), createCannonAmmoStack(ModBlocks.WIND_BOMB.get()));
        dropSelf(ModBlocks.EXPLOSIVE_BARREL.get());
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
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
