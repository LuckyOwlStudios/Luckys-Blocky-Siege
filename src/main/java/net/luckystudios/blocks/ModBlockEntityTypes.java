package net.luckystudios.blocks;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.cannon.CannonBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;


import java.util.function.Supplier;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BlockySiege.MOD_ID);

    public static final Supplier<BlockEntityType<CannonBlockEntity>> CANNON_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("cannon_block_entity", () -> BlockEntityType.Builder.of(
                    CannonBlockEntity::new, ModBlocks.CANNON.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
