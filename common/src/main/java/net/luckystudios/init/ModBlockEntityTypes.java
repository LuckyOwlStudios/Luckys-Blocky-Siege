package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerBlockEntity;
import net.luckystudios.blocks.custom.shooting.cannon.CannonBlockEntity;
import net.luckystudios.blocks.custom.shooting.multi_cannon.MultiCannonBlockEntity;
import net.luckystudios.blocks.custom.shooting.volley.VolleyRackBlockEntity;
import net.luckystudios.blocks.custom.turret.ballista.BallistaBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.Services;

import java.util.function.Supplier;

public class ModBlockEntityTypes {
    public static final Supplier<BlockEntityType<CannonBlockEntity>> CANNON_BLOCK_ENTITY =
            register("cannon_block_entity", () -> BlockEntityType.Builder.of(
                    CannonBlockEntity::new, ModBlocks.CANNON.get()).build(null));

    public static final Supplier<BlockEntityType<MultiCannonBlockEntity>> MULTI_CANNON_BLOCK_ENTITY =
            register("multi_cannon_block_entity", () -> BlockEntityType.Builder.of(
                    MultiCannonBlockEntity::new, ModBlocks.MULTI_CANNON.get()).build(null));

    public static final Supplier<BlockEntityType<SpewerBlockEntity>> SPEWER_BLOCK_ENTITY =
            register("spewer_cannon_block_entity", () -> BlockEntityType.Builder.of(
                    SpewerBlockEntity::new, ModBlocks.SPEWER_CANNON.get()).build(null));

    public static final Supplier<BlockEntityType<VolleyRackBlockEntity>> VOLLEY_BLOCK_ENTITY =
            register("volley_block_entity", () -> BlockEntityType.Builder.of(
                    VolleyRackBlockEntity::new, ModBlocks.VOLLEY_RACK.get()).build(null));

    public static final Supplier<BlockEntityType<BallistaBlockEntity>> BALLISTA_BLOCK_ENTITY =
            register("ballista_block_entity", () -> BlockEntityType.Builder.of(
                    BallistaBlockEntity::new, ModBlocks.BALLISTA_BLOCK.get()).build(null));

    private static <T extends BlockEntityType<?>> RegistryObject<T> register(String name, Supplier<T> factory) {
        return Services.REGISTRY.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, name, factory);
    }

    public static void init() {
    }
}
