package net.luckystudios.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public class ModCyclingSlotBackground {
    public List<ResourceLocation> icons = List.of();
    private int tick;
    private int iconIndex;

    public ModCyclingSlotBackground() {
    }

    public void tick(List<ResourceLocation> icons) {
        if (!this.icons.equals(icons)) {
            this.icons = icons;
            this.iconIndex = 0;
            this.tick = 0; // Reset tick when icons change
        }

        if (!this.icons.isEmpty()) {
            ++this.tick;
            if (this.tick % 30 == 0) {
                this.iconIndex = (this.iconIndex + 1) % this.icons.size();
            }
        }
    }

    public void render(AbstractContainerMenu containerMenu, GuiGraphics guiGraphics, float partialTick, int x, int y) {
        if (this.icons.isEmpty()) return;

        // Only show transition if we have multiple icons and enough time has passed
        boolean inTransition = this.icons.size() > 1 && (this.tick % 30) < 4 && this.tick >= 30;

        if (inTransition) {
            // Calculate transition alpha
            float transitionAlpha = this.getIconTransitionTransparency(partialTick);

            // Render previous icon (fading out)
            int previousIndex = Math.floorMod(this.iconIndex - 1, this.icons.size());
            this.renderIcon(previousIndex, 1.0F - transitionAlpha, guiGraphics, x, y);

            // Render current icon (fading in)
            this.renderIcon(this.iconIndex, transitionAlpha, guiGraphics, x, y);
        } else {
            // No transition, render current icon at full alpha
            this.renderIcon(this.iconIndex, 1.0F, guiGraphics, x, y);
        }
    }

    private void renderIcon(int iconIndex, float alpha, GuiGraphics guiGraphics, int x, int y) {
        if (iconIndex >= 0 && iconIndex < this.icons.size()) {
            ResourceLocation icon = this.icons.get(iconIndex);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
            guiGraphics.blit(icon, x, y, 0, 0, 16, 16, 16, 16);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset color to normal
        }
    }

    private float getIconTransitionTransparency(float partialTick) {
        float f = (float)(this.tick % 30) + partialTick;
        return Math.min(f, 4.0F) / 4.0F;
    }
}