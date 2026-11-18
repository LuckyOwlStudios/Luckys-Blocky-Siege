package net.luckystudios.gui.ballista;

import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class CrossbowSlotItemHandler extends Slot {
    public CrossbowSlotItemHandler(Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof CrossbowItem;
    }
}
