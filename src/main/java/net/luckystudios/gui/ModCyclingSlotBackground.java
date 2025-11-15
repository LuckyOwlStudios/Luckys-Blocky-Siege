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
        }

        if (!this.icons.isEmpty() && ++this.tick % 30 == 0) {
            this.iconIndex = (this.iconIndex + 1) % this.icons.size();
        }
    }

    public void render(AbstractContainerMenu containerMenu, GuiGraphics guiGraphics, float partialTick, int x, int y) {
            boolean flag = this.icons.size() > 1 && this.tick >= 30;
            float f = flag ? this.getIconTransitionTransparency(partialTick) : 1.0F;
            if (f < 1.0F) {
                int i = Math.floorMod(this.iconIndex - 1, this.icons.size());
                this.renderIcon(i, 1.0F - f, guiGraphics, x, y);
            }

            this.renderIcon(this.iconIndex, f, guiGraphics, x, y);
    }

    private void renderIcon(int iconIndex, float alpha, GuiGraphics guiGraphics, int x, int y) {
        ResourceLocation image = iconIndex == 1 ? ResourceLocation.parse("blockysiege:textures/gui/container/bottle.png") : ResourceLocation.parse("blockysiege:textures/gui/container/bucket.png");
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha); // Use the alpha parameter here
        guiGraphics.blit(image, x, y, 0, 0, 16, 16, 16, 16);
//        guiGraphics.blit(icon, x, y, 0, 0, 16, 16);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset color to normal
    }

//    private void renderIcon(ResourceLocation icon, float alpha, GuiGraphics guiGraphics, int x, int y) {
//        TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(icon);
//        guiGraphics.blit(x, y, 0, 16, 16, textureatlassprite, 1.0F, 1.0F, 1.0F, alpha);
//    }

    private float getIconTransitionTransparency(float partialTick) {
        float f = (float)(this.tick % 30) + partialTick;
        return Math.min(f, 4.0F) / 4.0F;
    }
}
