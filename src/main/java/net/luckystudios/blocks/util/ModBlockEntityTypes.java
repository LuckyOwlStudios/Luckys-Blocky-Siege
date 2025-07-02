package net.luckystudios.blocks.util;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.ModBlocks;
import net.luckystudios.blocks.custom.cannon.types.generic.CannonBlockEntity;
import net.luckystudios.blocks.custom.cannon.types.multi.MultiCannonBlockEntity;
import net.luckystudios.blocks.custom.iron_gate.IronGateBlockEntity;
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

    public static final Supplier<BlockEntityType<MultiCannonBlockEntity>> MULTI_CANNON_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("multi_cannon_block_entity", () -> BlockEntityType.Builder.of(
                    MultiCannonBlockEntity::new, ModBlocks.MULTI_CANNON.get()).build(null));

    public static final Supplier<BlockEntityType<IronGateBlockEntity>> IRON_GATE =
            BLOCK_ENTITIES.register("iron_gate", () -> BlockEntityType.Builder.of(
                    IronGateBlockEntity::new, ModBlocks.IRON_GATE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
