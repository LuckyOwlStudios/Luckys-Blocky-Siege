package net.luckystudios.gui.ballista;

import net.luckystudios.blocks.custom.turret.ballista.BallistaBlockEntity;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public class ArrowSlotItemHandler extends Slot {
    public ArrowSlotItemHandler(Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.is(ItemTags.ARROWS) || stack.getItem() instanceof ArrowItem || stack.getItem() instanceof FireworkRocketItem) {
            return true;
        }
        // Accept whatever ammo the installed (possibly modded) crossbow can fire.
        ItemStack crossbow = this.container.getItem(BallistaBlockEntity.CROSSBOW_SLOT);
        return crossbow.getItem() instanceof ProjectileWeaponItem weapon && weapon.getAllSupportedProjectiles().test(stack);
    }
}
