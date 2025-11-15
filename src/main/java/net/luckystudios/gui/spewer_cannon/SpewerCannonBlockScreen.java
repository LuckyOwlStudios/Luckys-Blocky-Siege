package net.luckystudios.gui.spewer_cannon;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerCannonBlockEntity;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class SpewerCannonBlockScreen extends AbstractContainerScreen<SpewerCannonBlockBlockMenu> {

    private final Level level;
    private final BlockPos blockPos;
    private final SpewerCannonBlockEntity spewerBlockEntity;
    private ImageButton targetButton;
    private final ModCyclingSlotBackground ammoIcon = new ModCyclingSlotBackground();

    // GUI Resources
    private static final ResourceLocation GUI_TEXTURE = BlockySiege.id("textures/gui/container/spewer_cannon_gui.png");
    private static final ResourceLocation LAVA_TANK_TEXTURE = BlockySiege.id("textures/gui/container/lava_tank.png");
    private static final ResourceLocation WATER_TANK_TEXTURE = BlockySiege.id("textures/gui/container/water_tank.png");
    private static final ResourceLocation POTION_TANK_TEXTURE = BlockySiege.id("textures/gui/container/potion_tank.png");

    private static final WidgetSprites TARGET_BUTTON_SPRITES = new WidgetSprites(
            BlockySiege.id("textures/gui/container/target.png"),
            BlockySiege.id("textures/gui/container/target_highlighted.png")
    );

    private static final List<ResourceLocation> SLOT_SPRITES = List.of(
            BlockySiege.id("textures/gui/container/bucket.png"),
            BlockySiege.id("textures/gui/container/bottle.png")
    );

    // GUI Layout Constants
    private static final int TANK_X = 40;
    private static final int TANK_Y = 17;
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 50;
    private static final int SLOT_ICON_X = 80;
    private static final int SLOT_ICON_Y = 35;
    private static final int TARGET_BUTTON_X = 156;
    private static final int TARGET_BUTTON_Y = 6;

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
                this.leftPos + TARGET_BUTTON_X,
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

        // Render slot icons if slot is empty
        if (spewerBlockEntity.getItem(0).isEmpty()) {
            ammoIcon.render(menu, guiGraphics, pPartialTick, x + SLOT_ICON_X, y + SLOT_ICON_Y);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private void renderFluidTank(GuiGraphics guiGraphics, int guiX, int guiY) {
        float fillPercentage = spewerBlockEntity.getFillPercentage();
        if (fillPercentage <= 0) return;

        int fillHeight = Mth.ceil(fillPercentage * TANK_HEIGHT);
        int emptyHeight = TANK_HEIGHT - fillHeight;

        FluidStack fluidStack = spewerBlockEntity.getFluidTank().getFluid();
        Fluid fluid = fluidStack.getFluid();

        // Set up rendering state
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Determine texture and color
        ResourceLocation texture = getTankTexture(fluid, fluidStack);
        setFluidColor(fluid, fluidStack);

        // Animation variables
        long gameTime = spewerBlockEntity.getLevel().getGameTime();
        float animationSpeed = 0.5f; // Adjust speed as needed
        float textureOffset = (gameTime * animationSpeed) % TANK_HEIGHT;

        // Render animated fluid
        renderAnimatedFluid(guiGraphics, texture,
                guiX + TANK_X, guiY + TANK_Y + emptyHeight,
                TANK_WIDTH, fillHeight, textureOffset);

        // Reset rendering state
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderAnimatedFluid(GuiGraphics guiGraphics, ResourceLocation texture,
                                     int x, int y, int width, int height, float offset) {

        // Calculate how many times we need to repeat the texture
        int textureHeight = TANK_HEIGHT; // Your texture height
        int totalHeight = height + (int)Math.ceil(offset);

        for (int i = 0; i < Math.ceil((float)totalHeight / textureHeight) + 1; i++) {
            int renderY = y - (int)offset + (i * textureHeight);
            int renderHeight = Math.min(textureHeight, Math.max(0, y + height - renderY));

            if (renderHeight <= 0) continue;

            // Calculate texture coordinates
            int vOffset = 0;
            if (renderY < y) {
                vOffset = y - renderY;
                renderHeight -= vOffset;
                renderY = y;
            }

            if (renderY + renderHeight > y + height) {
                renderHeight = (y + height) - renderY;
            }

            if (renderHeight > 0) {
                guiGraphics.blit(texture,
                        x, renderY,                    // screen position
                        0, vOffset,                    // texture u,v
                        width, renderHeight,           // render size
                        width, textureHeight);         // texture size
            }
        }
    }

    private ResourceLocation getTankTexture(Fluid fluid, FluidStack fluidStack) {
        if (fluid == Fluids.LAVA) {
            return LAVA_TANK_TEXTURE;
        } else if (fluidStack.has(DataComponents.POTION_CONTENTS)) {
            return POTION_TANK_TEXTURE;
        } else {
            return WATER_TANK_TEXTURE;
        }
    }

    private void setFluidColor(Fluid fluid, FluidStack fluidStack) {
        if (fluid == Fluids.LAVA) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // No tinting for lava
        } else {
            PotionContents potionContents = fluidStack.get(DataComponents.POTION_CONTENTS);
            if (potionContents != null) {
                int color = potionContents.getColor();
                float r = ((color >> 16) & 0xFF) / 255.0F;
                float g = ((color >> 8) & 0xFF) / 255.0F;
                float b = (color & 0xFF) / 255.0F;
                RenderSystem.setShaderColor(r, g, b, 1.0F);
            } else {
                // Default water color
                RenderSystem.setShaderColor(0.247F, 0.463F, 0.894F, 1.0F);
            }
        }
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
            Component tooltip = switch (fluidAmount) {
                case 0 -> Component.translatable("container.blockysiege.empty");
                case 1000 -> Component.translatable("container.blockysiege.full");
                default -> Component.literal(fluidAmount + " mB");
            };
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