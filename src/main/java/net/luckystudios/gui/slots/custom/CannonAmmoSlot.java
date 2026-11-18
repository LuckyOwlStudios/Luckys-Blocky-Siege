package net.luckystudios.gui.slots.custom;

import net.luckystudios.gui.slots.ModSlot;
import net.luckystudios.blocks.custom.shooting.cannon.CannonBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CannonAmmoSlot extends ModSlot {

    public CannonAmmoSlot(Container inventory, int index, int xPosition, int yPosition, AbstractContainerMenu menu) {
        super(inventory, index, xPosition, yPosition, menu);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return CannonBlockEntity.isValidAmmo(stack);
    }
}
