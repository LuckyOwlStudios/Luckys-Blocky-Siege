package net.luckystudios.gui.slots;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

public class ModSlot extends Slot {

    private final Runnable onChange;

    public ModSlot(Container inventory, int index, int xPosition, int yPosition, AbstractContainerMenu menu) {
        super(inventory, index, xPosition, yPosition);
        this.onChange = () -> menu.slotsChanged(inventory);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onChange.run();
    }
}
