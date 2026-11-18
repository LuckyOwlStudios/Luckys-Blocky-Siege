package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.Services;

import java.util.function.Supplier;

public class ModFluids {
    public static final Supplier<FlowingFluid> SOURCE_POTION = Services.REGISTRY.register(BuiltInRegistries.FLUID, "potion",
            Services.PLATFORM::createPotionSourceFluid);

    public static final Supplier<FlowingFluid> FLOWING_POTION = Services.REGISTRY.register(BuiltInRegistries.FLUID, "flowing_potion",
            Services.PLATFORM::createPotionFlowingFluid);

    public static void init() {
    }
}
