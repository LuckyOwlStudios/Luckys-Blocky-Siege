package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.luckystudios.fluids.PotionFluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, BlockySiege.MOD_ID);

    public static final Supplier<FlowingFluid> SOURCE_POTION = FLUIDS.register("potion",
            PotionFluid.Source::new);

    public static final Supplier<FlowingFluid> FLOWING_POTION = FLUIDS.register("flowing_potion",
            PotionFluid.Flowing::new);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
