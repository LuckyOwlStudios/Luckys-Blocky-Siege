package net.luckystudios.fabric.platform;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.luckystudios.BlockySiege;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.services.IRegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Fabric registers everything straight away, in the order the common code asks for it.
 */
public class FabricRegistryHelper implements IRegistryHelper {

    @Override
    public <R, T extends R> RegistryObject<T> register(Registry<R> registry, String name, Supplier<T> factory) {
        ResourceLocation id = BlockySiege.id(name);
        T value = Registry.register(registry, id, factory.get());
        return new Registered<>(id, value);
    }

    @Override
    public RegistryObject<PoiType> registerPoi(String name, Supplier<? extends Block> block, int maxTickets, int validRange) {
        ResourceLocation id = BlockySiege.id(name);
        // Also fills in the block state to POI lookup, which plain registration would skip.
        PoiType poiType = PointOfInterestHelper.register(id, maxTickets, validRange, block.get());
        return new Registered<>(id, poiType);
    }

    private record Registered<T>(ResourceLocation id, T value) implements RegistryObject<T> {
        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public T get() {
            return value;
        }
    }
}
