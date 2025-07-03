package net.luckystudios.gui.spewer_cannon;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LiquidSlotItemHandler extends SlotItemHandler {
    public LiquidSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(Tags.Items.BUCKETS_WATER) ||
               stack.is(Tags.Items.BUCKETS_LAVA) ||
               stack.is(Tags.Items.BUCKETS_POWDER_SNOW) ||
               stack.getItem() instanceof PotionItem;
    }
}
