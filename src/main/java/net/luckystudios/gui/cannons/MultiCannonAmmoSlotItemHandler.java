package net.luckystudios.gui.cannons;

import net.luckystudios.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class MultiCannonAmmoSlotItemHandler extends SlotItemHandler {
    public MultiCannonAmmoSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ModItems.BULLET.asItem());
    }
}
