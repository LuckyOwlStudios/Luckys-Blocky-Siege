package net.luckystudios.gui.slots.custom;

import net.luckystudios.gui.slots.ModSlot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;

public class LiquidSlot extends ModSlot {

    public LiquidSlot(Container inventory, int index, int xPosition, int yPosition, AbstractContainerMenu menu) {
        super(inventory, index, xPosition, yPosition, menu);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Any container that exposes a fluid handler (vanilla and modded buckets, tanks, etc.),
        // full to fill the spewer or empty to drain it, plus potions and empty bottles.
        return stack.getCapability(Capabilities.FluidHandler.ITEM) != null ||
               stack.is(Tags.Items.BUCKETS_POWDER_SNOW) ||
               stack.is(Items.GLASS_BOTTLE) ||
               stack.getItem() instanceof PotionItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
