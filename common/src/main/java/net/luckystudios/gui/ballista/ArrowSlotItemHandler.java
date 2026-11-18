package net.luckystudios.gui.ballista;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class ArrowSlotItemHandler extends Slot {
    public ArrowSlotItemHandler(Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ItemTags.ARROWS) || stack.getItem() instanceof FireworkRocketItem;
    }
}
