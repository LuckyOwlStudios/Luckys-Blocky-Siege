package net.luckystudios.neoforge;

import net.luckystudios.fluids.FluidTank;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Exposes a loader-independent {@link FluidTank} to NeoForge's fluid handler API. */
public class NeoForgeFluidHandler implements IFluidHandler {
    private final FluidTank tank;

    public NeoForgeFluidHandler(FluidTank tank) {
        this.tank = tank;
    }

    public static FluidStack toNeo(net.luckystudios.fluids.FluidStack stack) {
        if (stack.isEmpty()) return FluidStack.EMPTY;
        FluidStack result = new FluidStack(stack.getFluid(), stack.getAmount());
        result.applyComponents(stack.getComponentsPatch());
        return result;
    }

    public static net.luckystudios.fluids.FluidStack toCommon(FluidStack stack) {
        if (stack.isEmpty()) return net.luckystudios.fluids.FluidStack.EMPTY;
        return new net.luckystudios.fluids.FluidStack(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch());
    }

    private static FluidTank.FluidAction convert(FluidAction action) {
        return action.execute() ? FluidTank.FluidAction.EXECUTE : FluidTank.FluidAction.SIMULATE;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int index) {
        return toNeo(tank.getFluid());
    }

    @Override
    public int getTankCapacity(int index) {
        return tank.getCapacity();
    }

    @Override
    public boolean isFluidValid(int index, FluidStack stack) {
        return stack.getFluid() != Fluids.EMPTY;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return tank.fill(toCommon(resource), convert(action));
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, toNeo(tank.getFluid()))) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return toNeo(tank.drain(maxDrain, convert(action)));
    }
}
