package net.luckystudios.gui.slots.custom;

import net.luckystudios.gui.slots.ModSlot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.luckystudios.init.ModTags;

public class LiquidSlot extends ModSlot {

    public LiquidSlot(Container inventory, int index, int xPosition, int yPosition, AbstractContainerMenu menu) {
        super(inventory, index, xPosition, yPosition, menu);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ModTags.BUCKETS_WATER) ||
               stack.is(ModTags.BUCKETS_LAVA) ||
               stack.is(ModTags.BUCKETS_POWDER_SNOW) ||
               stack.getItem() instanceof PotionItem;
    }
}
