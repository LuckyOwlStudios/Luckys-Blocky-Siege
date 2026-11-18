package net.luckystudios.gui.spewer_cannon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Matrix4f;
import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerBlockEntity;
import net.luckystudios.gui.ModCyclingSlotBackground;
import net.luckystudios.networking.SpewerCannonBlockScreenPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

// For some reason, we can only use textures directly inside the container folder.
// Can't make sub-folders, can't use any other directory.
// Nothing else.

public class SpewerCannonBlockScreen extends AbstractContainerScreen<SpewerCannonBlockBlockMenu> {

    private final Level level;
    private final BlockPos blockPos;
    private final SpewerBlockEntity spewerBlockEntity;
    private ImageButton targetButton;
    private final ModCyclingSlotBackground ammoIcon = new ModCyclingSlotBackground();

    // GUI Resources
    private static final ResourceLocation GUI_TEXTURE = BlockySiege.id("textures/gui/container/spewer_cannon_gui.png");

    private static final WidgetSprites TARGET_BUTTON_SPRITES = new WidgetSprites(
            BlockySiege.id("textures/gui/container/target.png"),
            BlockySiege.id("textures/gui/container/target_highlighted.png")
    );

    private static final List<ResourceLocation> SLOT_SPRITES = List.of(
            BlockySiege.id("textures/gui/container/bucket.png"),
            BlockySiege.id("textures/gui/container/bottle.png")
    );

    // GUI Layout Constants
    private static final int TANK_X = 43;
    private static final int TANK_Y = 20;
    private static final int TANK_WIDTH = 12;
    private static final int TANK_HEIGHT = 44;
    private static final int SLOT_ICON_X = 80;
    private static final int SLOT_ICON_Y = 35;
    private static final int BUTTONS_X = 152;
    private static final int TARGET_BUTTON_Y = 10;

    public SpewerCannonBlockScreen(SpewerCannonBlockBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.level = playerInventory.player.level();
        this.blockPos = menu.blockEntity.getBlockPos();
        this.spewerBlockEntity = menu.blockEntity;
        this.ammoIcon.icons = SLOT_SPRITES;
    }

    @Override
    protected void init() {
        super.init();
        this.targetButton = new ImageButton(
                this.leftPos + BUTTONS_X,
                this.topPos + TARGET_BUTTON_Y,
                14, 14,
                TARGET_BUTTON_SPRITES,
                button -> PacketDistributor.sendToServer(new SpewerCannonBlockScreenPacket(blockPos, 0, 0))
        ) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
            }
        };
        this.addRenderableWidget(targetButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render main GUI background
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Render fluid tank
        renderFluidTank(guiGraphics, x, y);

        RenderSystem.enableBlend();

        // Render slot icons if slot is empty
        if (spewerBlockEntity.getItem(0).isEmpty()) {
            ammoIcon.render(menu, guiGraphics, pPartialTick, x + SLOT_ICON_X, y + SLOT_ICON_Y);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private void renderFluidTank(GuiGraphics guiGraphics, int guiX, int guiY) {
        FluidStack fluidStack = spewerBlockEntity.getFluidTank().getFluid();
        if (fluidStack.isEmpty()) return;

        int capacity = spewerBlockEntity.getFluidTank().getCapacity();
        int fillHeight = Mth.clamp(Mth.ceil((float) fluidStack.getAmount() / capacity * TANK_HEIGHT), 1, TANK_HEIGHT);

        // Any fluid (vanilla or modded) supplies its own still texture and tint through its client extensions
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ext.getStillTexture(fluidStack));

        int tint = ext.getTintColor(fluidStack);
        PotionContents potion = fluidStack.get(DataComponents.POTION_CONTENTS);
        if (potion != null) tint = 0xFF000000 | potion.getColor();
        float a = ((tint >> 24) & 0xFF) / 255.0F;
        float r = ((tint >> 16) & 0xFF) / 255.0F;
        float g = ((tint >> 8) & 0xFF) / 255.0F;
        float b = (tint & 0xFF) / 255.0F;

        // Tile the 16x16 sprite from the bottom of the tank upward
        int left = guiX + TANK_X;
        int bottom = guiY + TANK_Y + TANK_HEIGHT;
        RenderSystem.enableBlend();
        for (int drawn = 0; drawn < fillHeight; drawn += 16) {
            int h = Math.min(16, fillHeight - drawn);
            for (int dx = 0; dx < TANK_WIDTH; dx += 16) {
                int w = Math.min(16, TANK_WIDTH - dx);
                blitSprite(guiGraphics, sprite, left + dx, bottom - drawn - h, w, h, r, g, b, a);
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    // Draws the top-left w x h part of a sprite, tinted. Cropping UVs keeps the texture undistorted.
    private static void blitSprite(GuiGraphics guiGraphics, TextureAtlasSprite sprite, int x, int y, int w, int h,
                                   float r, float g, float b, float a) {
        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = u0 + (sprite.getU1() - u0) * w / 16.0F;
        float v1 = v0 + (sprite.getV1() - v0) * h / 16.0F;
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(matrix, x, y + h, 0).setUv(u0, v1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + w, y + h, 0).setUv(u1, v1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + w, y, 0).setUv(u1, v0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y, 0).setUv(u0, v0).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ammoIcon.tick(SLOT_SPRITES);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(guiGraphics, pMouseX, pMouseY);

        // Render tooltips
        if (targetButton.isHovered()) {
            guiGraphics.renderTooltip(font, Component.translatable("container.blockysiege.aim"), pMouseX, pMouseY);
        }

        renderTankTooltip(guiGraphics, pMouseX, pMouseY);
    }

    private void renderTankTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Check if mouse is over tank area
        int tankLeft = leftPos + TANK_X - 2;
        int tankRight = leftPos + TANK_X + TANK_WIDTH + 2;
        int tankTop = topPos + TANK_Y - 1;
        int tankBottom = topPos + TANK_Y + TANK_HEIGHT + 1;

        if (mouseX >= tankLeft && mouseX <= tankRight && mouseY >= tankTop && mouseY <= tankBottom) {
            int fluidAmount = spewerBlockEntity.getFluidTank().getFluidAmount();

            if (fluidAmount == 0) {
                Component tooltip = Component.translatable("container.blockysiege.empty");
                guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
                return;
            }

            FluidStack fluidStack = spewerBlockEntity.getFluidTank().getFluid();
            Component tooltip = Component.empty().append(fluidStack.getHoverName()).append(": " + fluidAmount + "mB");
            guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Center the title
        int titleX = (imageWidth - font.width(title)) / 2;
        guiGraphics.drawString(font, title, titleX, titleLabelY, 4210752, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }
}