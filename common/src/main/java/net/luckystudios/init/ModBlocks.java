package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.cannon_ammo.PileBlock;
import net.luckystudios.blocks.custom.cannon_ammo.types.*;
import net.luckystudios.blocks.custom.shooting.cannon.CannonBlock;
import net.luckystudios.blocks.custom.shooting.multi_cannon.MultiCannonBlock;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerBlock;
import net.luckystudios.blocks.custom.explosive_barrel.ExplosiveBarrelBlock;
import net.luckystudios.blocks.custom.shooting.volley.VolleyRackBlock;
import net.luckystudios.blocks.custom.turret.ballista.BallistaBlock;
import net.luckystudios.blocks.util.ModBlockStateProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final RegistryObject<Block> CANNON = registerBlock("cannon",
            () -> new CannonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .dynamicShape()
                    .requiresCorrectToolForDrops()
                    .lightLevel(firingStateBlockEmission())
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.ANVIL)
            ));

    public static final RegistryObject<Block> MULTI_CANNON = registerBlock("multi_cannon",
            () -> new MultiCannonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .dynamicShape()
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.ANVIL)
            ));

    public static final RegistryObject<Block> SPEWER_CANNON = registerBlock("spewer_cannon",
            () -> new SpewerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .dynamicShape()
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.ANVIL)
            ));

    public static final RegistryObject<Block> VOLLEY_RACK = registerBlock("volley_rack",
            () -> new VolleyRackBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .dynamicShape()
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
            ));

    public static final RegistryObject<Block> CANNONBALL = registerBlock("cannonball",
            () -> new CannonBallBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));
    public static final RegistryObject<Block> SPECTRAL_CANNONBALL = registerBlock("spectral_cannonball",
            () -> new CannonBallBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .lightLevel(FireBombBlock.LIGHT_EMISSION)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));
    public static final RegistryObject<Block> FIRE_BOMB = registerBlock("fire_bomb",
            () -> new FireBombBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE)
                    .lightLevel(FireBombBlock.LIGHT_EMISSION)
                    .strength(0.3F, 0.3F)
                    .sound(SoundType.DECORATED_POT)
            ));
    public static final RegistryObject<Block> FROST_BOMB = registerBlock("frost_bomb",
            () -> new FrostBombBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE)
                    .strength(0.3F, 0.3F)
                    .sound(SoundType.DECORATED_POT)
            ));
    public static final RegistryObject<Block> WIND_BOMB = registerBlock("wind_bomb",
            () -> new WindBombBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_WHITE)
                    .strength(0.3F, 0.3F)
                    .sound(SoundType.DECORATED_POT)
            ));

    public static final RegistryObject<Block> EMBER_PILE = registerBlock("ember_pile",
            () -> new PileBlock(PileBlock.PileType.FIRE, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE)
                    .lightLevel(blockState -> 8)
                    .sound(SoundType.STONE)
            ));

    public static final RegistryObject<Block> FROST_PILE = registerBlock("frost_pile",
            () -> new PileBlock(PileBlock.PileType.FROST, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE)
                    .instabreak()
                    .sound(SoundType.POWDER_SNOW)
            ));

    public static final RegistryObject<Block> EXPLOSIVE_BARREL = registerBlock("explosive_barrel",
            () -> new ExplosiveBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS)));

    public static final RegistryObject<Block> BALLISTA_BLOCK = registerBlock("ballista_block",
            () -> new BallistaBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .dynamicShape()
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));

    private static ToIntFunction<BlockState> firingStateBlockEmission() {
        return state -> switch (state.getValue(ModBlockStateProperties.FIRING_STATE)) {
            case OFF -> 0;
            case CHARGING -> 5;
            case FIRED -> 10;
        };
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = Services.REGISTRY.register(BuiltInRegistries.BLOCK, name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        Services.REGISTRY.register(BuiltInRegistries.ITEM, name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void init() {
    }
}
