package net.luckystudios.fluids;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * A single-fluid tank, independent of the mod loader. Mirrors the parts of NeoForge's FluidTank the mod uses
 * and saves in the same NBT layout.
 */
public class FluidTank {

    public enum FluidAction {
        EXECUTE,
        SIMULATE;

        public boolean execute() {
            return this == EXECUTE;
        }
    }

    private final int capacity;
    private FluidStack fluid = FluidStack.EMPTY;

    public FluidTank(int capacity) {
        this.capacity = capacity;
    }

    /** Called whenever the contents change. */
    protected void onContentsChanged() {
    }

    public int getCapacity() {
        return capacity;
    }

    public FluidStack getFluid() {
        return fluid;
    }

    public void setFluid(FluidStack stack) {
        this.fluid = stack;
    }

    public int getFluidAmount() {
        return fluid.getAmount();
    }

    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    public int getSpace() {
        return Math.max(0, capacity - fluid.getAmount());
    }

    /** Returns how much of {@code resource} went (or would go) into the tank. */
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        if (fluid.isEmpty()) {
            int filled = Math.min(capacity, resource.getAmount());
            if (action.execute()) {
                fluid = resource.copyWithAmount(filled);
                onContentsChanged();
            }
            return filled;
        }
        if (!FluidStack.isSameFluidSameComponents(fluid, resource)) {
            return 0;
        }
        int filled = Math.min(capacity - fluid.getAmount(), resource.getAmount());
        if (filled > 0 && action.execute()) {
            fluid.grow(filled);
            onContentsChanged();
        }
        return filled;
    }

    /** Removes up to {@code maxDrain} millibuckets and returns what was (or would be) removed. */
    public FluidStack drain(int maxDrain, FluidAction action) {
        int drained = Math.min(fluid.getAmount(), maxDrain);
        FluidStack stack = fluid.copyWithAmount(drained);
        if (action.execute() && drained > 0) {
            fluid.shrink(drained);
            if (fluid.getAmount() <= 0) {
                fluid = FluidStack.EMPTY;
            }
            onContentsChanged();
        }
        return stack;
    }

    public FluidTank readFromNBT(HolderLookup.Provider registries, CompoundTag tag) {
        setFluid(FluidStack.parseOptional(registries, tag.getCompound("Fluid")));
        return this;
    }

    public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag tag) {
        if (!fluid.isEmpty()) {
            tag.put("Fluid", fluid.save(registries));
        }
        return tag;
    }
}
