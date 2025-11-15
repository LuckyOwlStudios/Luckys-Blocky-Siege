package net.luckystudios.gui.slots.custom;

import net.luckystudios.gui.slots.ModSlot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

public class FuseSlot extends ModSlot {

    public FuseSlot(Container inventory, int index, int xPosition, int yPosition, AbstractContainerMenu menu) {
        super(inventory, index, xPosition, yPosition, menu);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(Tags.Items.GUNPOWDERS);
    }
}
