package net.luckystudios.gui.ballista;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ArrowSlotItemHandler extends SlotItemHandler {
    public ArrowSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ItemTags.ARROWS) || stack.getItem() instanceof FireworkRocketItem;
    }
}
