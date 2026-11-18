package net.luckystudios.fluids;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/** Moves fluid between a {@link FluidTank} and vanilla buckets. Used by loaders without their own fluid item API. */
public final class VanillaFluidContainers {
    private VanillaFluidContainers() {
    }

    public static boolean isContainer(ItemStack stack) {
        return stack.getItem() instanceof BucketItem;
    }

    @Nullable
    public static ItemStack emptyInto(ItemStack container, FluidTank tank) {
        if (!(container.getItem() instanceof BucketItem bucket) || bucket.content == Fluids.EMPTY) return null;
        FluidStack fluid = new FluidStack(bucket.content, 1000);
        if (tank.fill(fluid, FluidTank.FluidAction.SIMULATE) != 1000) return null;
        tank.fill(fluid, FluidTank.FluidAction.EXECUTE);
        Item remainder = bucket.getCraftingRemainingItem();
        return remainder == null ? ItemStack.EMPTY : new ItemStack(remainder);
    }

    @Nullable
    public static ItemStack fillFrom(ItemStack container, FluidTank tank) {
        if (!container.is(Items.BUCKET) || tank.getFluidAmount() < 1000) return null;
        FluidStack fluid = tank.getFluid();
        if (fluid.has(net.minecraft.core.component.DataComponents.POTION_CONTENTS)) return null;
        Item bucket = fluid.getFluid().getBucket();
        if (bucket == Items.AIR) return null;
        tank.drain(1000, FluidTank.FluidAction.EXECUTE);
        return new ItemStack(bucket);
    }

    public static SoundEvent sound(FluidStack fluid, boolean emptyingIntoTank) {
        boolean lava = fluid.getFluid() == Fluids.LAVA;
        if (emptyingIntoTank) return lava ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        return lava ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
    }
}
