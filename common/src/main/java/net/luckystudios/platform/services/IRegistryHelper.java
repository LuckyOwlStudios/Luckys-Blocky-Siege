package net.luckystudios.platform.services;

import net.luckystudios.platform.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public interface IRegistryHelper {

    /**
     * Registers an object under the mod's namespace. On NeoForge this goes through a DeferredRegister,
     * on Fabric the object is created and registered straight away.
     */
    <R, T extends R> RegistryObject<T> register(Registry<R> registry, String name, Supplier<T> factory);

    /**
     * Registers a point of interest made of every state of the given block. Loaders differ in how the
     * block state to POI lookup gets filled in, so this is its own method.
     */
    RegistryObject<PoiType> registerPoi(String name, Supplier<? extends Block> block, int maxTickets, int validRange);
}
