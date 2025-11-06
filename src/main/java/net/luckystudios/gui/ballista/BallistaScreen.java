package net.luckystudios.gui.ballista;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.turret.ballista.BallistaBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

// This is the image
public class BallistaScreen extends AbstractContainerScreen<BallistaMenu> {

    Level level;
    BlockPos blockPos;
    Player player;
    BallistaBlockEntity ballistaBlockEntity;

    private static final ResourceLocation GUI_TEXTURE =
            BlockySiege.id("textures/gui/container/ballista_gui.png");

    public BallistaScreen(BallistaMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.player = playerInventory.player;
        this.level = player.level();
        this.blockPos = menu.blockEntity.getBlockPos();
        this.ballistaBlockEntity = menu.blockEntity;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURE, x, y - 31, 0, 0, 256, 256);
        RenderSystem.disableBlend();
    }


    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(guiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Center the title at the top of the GUI
        int titleX = (this.imageWidth - this.font.width(this.title)) / 2;
        guiGraphics.drawString(this.font, this.title, titleX, this.titleLabelY - 32, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    protected void init() {
        super.init();

        // Position button relative to the GUI
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
    }
}
