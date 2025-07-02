package net.luckystudios.blocks;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.cannon.types.generic.CannonBlock;
import net.luckystudios.blocks.custom.cannon.types.multi.MultiCannonBlock;
import net.luckystudios.blocks.custom.cannon_ammo.*;
import net.luckystudios.blocks.custom.explosive_barrel.ExplosiveBarrelBlock;
import net.luckystudios.blocks.custom.iron_gate.IronGateBlock;
import net.luckystudios.items.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(BlockySiege.MOD_ID);

    public static final DeferredBlock<Block> CANNON = registerBlock("cannon",
            () -> new CannonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .dynamicShape()
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));

    public static final DeferredBlock<Block> MULTI_CANNON = registerBlock("multi_cannon",
            () -> new MultiCannonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .dynamicShape()
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));

    public static final DeferredBlock<Block> IRON_GATE = registerBlock("iron_gate",
            () -> new IronGateBlock(3, 5, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .dynamicShape()
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));

    public static final DeferredBlock<Block> CANNON_BALL = registerBlock("cannon_ball",
            () -> new CannonBallBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));
    public static final DeferredBlock<Block> EXPLOSIVE_KEG = registerBlock("explosive_keg",
            () -> new ExplosiveKegBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 2.0F)
                    .sound(SoundType.WOOD)
            ));
    public static final DeferredBlock<Block> FIRE_BOMB = registerBlock("fire_bomb",
            () -> new FireBombBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE)
                    .lightLevel(FireBombBlock.LIGHT_EMISSION)
                    .strength(0.3F, 0.3F)
                    .sound(SoundType.DECORATED_POT)
            ));
    public static final DeferredBlock<Block> FROST_BOMB = registerBlock("frost_bomb",
            () -> new FrostBombBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE)
                    .strength(0.3F, 0.3F)
                    .sound(SoundType.DECORATED_POT)
            ));
    public static final DeferredBlock<Block> WIND_BOMB = registerBlock("wind_bomb",
            () -> new WindBombBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_WHITE)
                    .strength(0.3F, 0.3F)
                    .sound(SoundType.DECORATED_POT)
            ));

    public static final DeferredBlock<Block> EXPLOSIVE_BARREL = registerBlock("explosive_barrel",
            () -> new ExplosiveBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
