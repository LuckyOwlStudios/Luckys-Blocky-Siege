package net.luckystudios.gui.cannons;

import net.luckystudios.init.ModTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CannonAmmoSlotItemHandler extends SlotItemHandler {
    public CannonAmmoSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ModTags.CANNON_AMMO);
    }
}
