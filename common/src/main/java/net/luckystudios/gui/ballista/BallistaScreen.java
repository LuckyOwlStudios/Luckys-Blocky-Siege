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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

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
        // Only the panel (176x197); the filled health bar sprite lives further down the texture.
        guiGraphics.blit(GUI_TEXTURE, x, y - 31, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        renderHealthBar(guiGraphics, x, y - 31);
        RenderSystem.disableBlend();
    }


    // Health bar: the empty bar is part of the background at (BAR_X, BAR_Y); the filled sprite sits at (BAR_U, BAR_V).
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 197;
    private static final int BAR_X = 8;
    private static final int BAR_Y = 24;
    private static final int BAR_U = 8;
    private static final int BAR_V = 200;
    private static final int BAR_WIDTH = 160;
    private static final int BAR_HEIGHT = 3;

    private void renderHealthBar(GuiGraphics guiGraphics, int originX, int originY) {
        int filled = Mth.ceil(BAR_WIDTH * this.menu.getHealthFraction());
        if (filled > 0) {
            guiGraphics.blit(GUI_TEXTURE, originX + BAR_X, originY + BAR_Y, BAR_U, BAR_V, filled, BAR_HEIGHT);
        }
    }

    private boolean isHoveringHealthBar(int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2 + BAR_X;
        int y = (height - imageHeight) / 2 - 31 + BAR_Y;
        return mouseX >= x - 1 && mouseX < x + BAR_WIDTH + 1 && mouseY >= y - 1 && mouseY < y + BAR_HEIGHT + 1;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(guiGraphics, pMouseX, pMouseY);
        if (isHoveringHealthBar(pMouseX, pMouseY)) {
            String health = String.format("%.1f / %.1f", this.menu.getHealth(), this.menu.getMaxHealth());
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.blockysiege.ballista.health", health), pMouseX, pMouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Center the title at the top of the GUI
        int titleX = (this.imageWidth - this.font.width(this.title)) / 2;
        guiGraphics.drawString(this.font, this.title, titleX, this.titleLabelY - 32, 4210752, false);
        renderTeamLabel(guiGraphics, titleX);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    private static final int LABEL_COLOR = 4210752;

    /** The ballista's scoreboard team, right-aligned in the title bar in the team's colour. */
    private void renderTeamLabel(GuiGraphics guiGraphics, int titleX) {
        PlayerTeam team = this.menu.blockEntity.findBallista()
                .map(ballista -> ballista.getTeam() instanceof PlayerTeam playerTeam ? playerTeam : null)
                .orElse(null);
        String text;
        int color = LABEL_COLOR;
        if (team != null) {
            text = team.getDisplayName().getString();
            Integer teamColor = team.getColor().getColor();
            if (teamColor != null) {
                color = teamColor;
            }
        } else {
            text = Component.translatable("gui.blockysiege.ballista.no_team").getString();
        }
        int right = this.imageWidth - 8;
        int maxWidth = right - (titleX + this.font.width(this.title) + 6);
        if (maxWidth <= 0) {
            return;
        }
        if (this.font.width(text) > maxWidth) {
            text = this.font.plainSubstrByWidth(text, maxWidth - this.font.width("...")) + "...";
        }
        guiGraphics.drawString(this.font, text, right - this.font.width(text), this.titleLabelY - 32, color, false);
    }

    @Override
    protected void init() {
        super.init();

        // Position button relative to the GUI
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
    }
}
