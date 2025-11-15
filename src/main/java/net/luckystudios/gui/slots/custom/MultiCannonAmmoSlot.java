package net.luckystudios.gui.slots.custom;

import net.luckystudios.gui.slots.ModSlot;
import net.luckystudios.init.ModTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class MultiCannonAmmoSlot extends ModSlot {

    public MultiCannonAmmoSlot(Container inventory, int index, int xPosition, int yPosition, AbstractContainerMenu menu) {
        super(inventory, index, xPosition, yPosition, menu);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ModTags.MULTI_CANNON_SHOOTABLE);
    }
}
