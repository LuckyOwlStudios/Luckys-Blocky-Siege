package net.luckystudios.blocks;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.BlunderbombBlock;
import net.luckystudios.blocks.custom.CannonBallBlock;
import net.luckystudios.blocks.custom.KegOfGunpowderBlock;
import net.luckystudios.blocks.custom.FrostBombBlock;
import net.luckystudios.blocks.custom.cannon.CannonBlock;
import net.luckystudios.items.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
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
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));

    public static final DeferredBlock<Block> CANNON_BALL = registerBlock("cannon_ball",
            () -> new CannonBallBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.METAL)
            ));
    public static final DeferredBlock<Block> KEG_OF_GUNPOWDER = registerBlock("keg_of_gunpowder",
            () -> new KegOfGunpowderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 2.0F)
                    .sound(SoundType.WOOD)
            ));
    public static final DeferredBlock<Block> BLUNDERBOMB = registerBlock("blunderbomb",
            () -> new BlunderbombBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .lightLevel(BlunderbombBlock.LIGHT_EMISSION)
                    .strength(0.3F, 0.3F)
                    .sound(SoundType.GLASS)
            ));
    public static final DeferredBlock<Block> FROST_BOMB = registerBlock("frost_bomb",
            () -> new FrostBombBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(0.3F, 0.3F)
                    .sound(SoundType.GLASS)
            ));

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
