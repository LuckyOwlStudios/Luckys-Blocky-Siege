package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, BlockySiege.MOD_ID);

    public static final Supplier<FluidType> POTION_FLUID_TYPE = FLUID_TYPES.register("potion",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.blockysiege.potion")
                    .fallDistanceModifier(0F)
                    .canExtinguish(true)
                    .canDrown(true)
                    .canHydrate(true)
                    .motionScale(0.014)
                    .density(1500)
                    .viscosity(1500)
                    .temperature(300)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
