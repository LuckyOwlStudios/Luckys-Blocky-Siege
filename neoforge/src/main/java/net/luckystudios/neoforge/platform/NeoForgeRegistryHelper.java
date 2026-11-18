package net.luckystudios.neoforge.platform;

import com.google.common.collect.ImmutableSet;
import net.luckystudios.BlockySiege;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.services.IRegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    private static final Map<ResourceKey<? extends Registry<?>>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();
    private static IEventBus modEventBus;

    /** Hooks every DeferredRegister, including ones created later, up to the mod event bus. */
    public static void registerAll(IEventBus eventBus) {
        modEventBus = eventBus;
        REGISTERS.values().forEach(register -> register.register(eventBus));
    }

    @SuppressWarnings("unchecked")
    private static <R> DeferredRegister<R> getOrCreate(Registry<R> registry) {
        return (DeferredRegister<R>) REGISTERS.computeIfAbsent(registry.key(), key -> {
            DeferredRegister<R> register = DeferredRegister.create(registry, BlockySiege.MOD_ID);
            if (modEventBus != null) {
                register.register(modEventBus);
            }
            return register;
        });
    }

    @Override
    public <R, T extends R> RegistryObject<T> register(Registry<R> registry, String name, Supplier<T> factory) {
        DeferredHolder<R, T> holder = getOrCreate(registry).register(name, factory);
        return new RegistryObject<>() {
            @Override
            public ResourceLocation getId() {
                return holder.getId();
            }

            @Override
            public T get() {
                return holder.get();
            }
        };
    }

    @Override
    public RegistryObject<PoiType> registerPoi(String name, Supplier<? extends Block> block, int maxTickets, int validRange) {
        // NeoForge fills in the block state to POI lookup itself.
        return register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, name,
                () -> new PoiType(ImmutableSet.copyOf(block.get().getStateDefinition().getPossibleStates()), maxTickets, validRange));
    }
}
