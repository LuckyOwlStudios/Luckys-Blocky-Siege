package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerBlockEntity;
import net.luckystudios.blocks.custom.shooting.cannon.CannonBlockEntity;
import net.luckystudios.blocks.custom.shooting.multi_cannon.MultiCannonBlockEntity;
import net.luckystudios.blocks.custom.shooting.volley.VolleyRackBlockEntity;
import net.luckystudios.blocks.custom.turret.ballista.BallistaBlockEntity;
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

    public static final Supplier<BlockEntityType<SpewerBlockEntity>> SPEWER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("spewer_cannon_block_entity", () -> BlockEntityType.Builder.of(
                    SpewerBlockEntity::new, ModBlocks.SPEWER_CANNON.get()).build(null));

    public static final Supplier<BlockEntityType<VolleyRackBlockEntity>> VOLLEY_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("volley_block_entity", () -> BlockEntityType.Builder.of(
                    VolleyRackBlockEntity::new, ModBlocks.VOLLEY_RACK.get()).build(null));

    public static final Supplier<BlockEntityType<BallistaBlockEntity>> BALLISTA_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ballista_block_entity", () -> BlockEntityType.Builder.of(
                    BallistaBlockEntity::new, ModBlocks.BALLISTA_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
