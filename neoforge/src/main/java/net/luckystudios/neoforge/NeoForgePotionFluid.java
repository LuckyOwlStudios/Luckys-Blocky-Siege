package net.luckystudios.neoforge;

import net.luckystudios.fluids.PotionFluid;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * NeoForge needs every modded fluid to name its FluidType.
 */
public final class NeoForgePotionFluid {
    private NeoForgePotionFluid() {
    }

    public static class Source extends PotionFluid.Source {
        @Override
        public FluidType getFluidType() {
            return ModFluidTypes.POTION_FLUID_TYPE.get();
        }
    }

    public static class Flowing extends PotionFluid.Flowing {
        @Override
        public FluidType getFluidType() {
            return ModFluidTypes.POTION_FLUID_TYPE.get();
        }
    }
}
