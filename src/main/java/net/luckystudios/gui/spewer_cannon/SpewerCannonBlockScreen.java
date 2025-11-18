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

// For some reason, we can only use textures directly inside the container folder.
// Can't make sub-folders, can't use any other directory.
// Nothing else.

public class SpewerCannonBlockScreen extends AbstractContainerScreen<SpewerCannonBlockBlockMenu> {

    private final Level level;
    private final BlockPos blockPos;
    private final SpewerCannonBlockEntity spewerBlockEntity;
    private ImageButton targetButton;
    private ImageButton infoButton;
    private final ModCyclingSlotBackground ammoIcon = new ModCyclingSlotBackground();

    // GUI Resources
    private static final ResourceLocation GUI_TEXTURE = BlockySiege.id("textures/gui/container/spewer_cannon_gui.png");
    private static final ResourceLocation LAVA_FRAME_TEXTURE = BlockySiege.id("textures/gui/container/lava_frame.png");
    private static final ResourceLocation LAVA_LIQUID_TEXTURE = BlockySiege.id("textures/gui/container/lava.png");
    private static final ResourceLocation WATER_FRAME_TEXTURE = BlockySiege.id("textures/gui/container/water_frame.png");
    private static final ResourceLocation WATER_LIQUID_TEXTURE = BlockySiege.id("textures/gui/container/water.png");
    private static final ResourceLocation POTION_TANK_TEXTURE = BlockySiege.id("textures/gui/container/potion.png");

    private static final WidgetSprites TARGET_BUTTON_SPRITES = new WidgetSprites(
            BlockySiege.id("textures/gui/container/target.png"),
            BlockySiege.id("textures/gui/container/target_highlighted.png")
    );

    private static final WidgetSprites INFO_BUTTON_SPRITES = new WidgetSprites(
            BlockySiege.id("textures/gui/container/info.png"),
            BlockySiege.id("textures/gui/container/info_highlighted.png")
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
    private static final int INFO_BUTTON_Y = 26;

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
        this.infoButton = new ImageButton(
                this.leftPos + BUTTONS_X,
                this.topPos + INFO_BUTTON_Y,
                14, 14,
                INFO_BUTTON_SPRITES,
                button -> PacketDistributor.sendToServer(new SpewerCannonBlockScreenPacket(blockPos, 0, 0))
        ) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
            }
        };
        this.addRenderableWidget(infoButton);
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
        float fillPercentage = spewerBlockEntity.getFillPercentage();
        if (fillPercentage <= 0) return;

        int fillHeight = Mth.ceil(fillPercentage * TANK_HEIGHT);
        int emptyHeight = TANK_HEIGHT - fillHeight;

        FluidStack fluidStack = spewerBlockEntity.getFluidTank().getFluid();
        Fluid fluid = spewerBlockEntity.getFluid();

        // Set up rendering state
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Determine texture and color
        ResourceLocation texture = getTankTexture(fluidStack);
        setFluidColor(fluidStack);

        // Animation variables
        long gameTime = spewerBlockEntity.getLevel().getGameTime();
        float animationSpeed = spewerBlockEntity.getFluid() == Fluids.LAVA ? 0.25F : 0.5f; // Adjust speed as needed
        float textureOffset = (gameTime * animationSpeed) % TANK_HEIGHT;

        // Render animated fluid
        renderAnimatedFluid(guiGraphics, texture,
                guiX + TANK_X, guiY + TANK_Y + emptyHeight,
                TANK_WIDTH, fillHeight, textureOffset);

        // Reset color before rendering frame
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Render static frame overlay
//        ResourceLocation frameTexture = getFrameTexture(fluid, fluidStack);
//        renderStaticFrame(guiGraphics, frameTexture,
//                guiX + TANK_X, guiY + TANK_Y + emptyHeight,
//                TANK_WIDTH, fillHeight);

        // Reset rendering state
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

//    private ResourceLocation getFrameTexture(Fluid fluid, FluidStack fluidStack) {
//        if (fluid == Fluids.LAVA) {
//            return LAVA_FRAME_TEXTURE;
//        } else {
//            // For both water and potions, use water frame
//            // You could add a separate potion frame texture if desired
//            return WATER_FRAME_TEXTURE;
//        }
//    }
//
//    private void renderStaticFrame(GuiGraphics guiGraphics, ResourceLocation frameTexture,
//                                   int x, int y, int width, int height) {
//        // Render frame at the same position and size as the liquid, but without animation
//        int textureHeight = TANK_HEIGHT; // Frame texture height should match liquid texture height
//
//        // If the fill height is less than the full texture height, we need to clip the frame
//        if (height < textureHeight) {
//            // Calculate which part of the frame texture to show (bottom portion)
//            int vOffset = textureHeight - height;
//            guiGraphics.blit(frameTexture,
//                    x, y,                      // screen position
//                    0, vOffset,                // texture u,v (start from bottom part of texture)
//                    width, height,             // render size
//                    width, textureHeight);     // texture size
//        } else {
//            // Full height - render normally
//            guiGraphics.blit(frameTexture,
//                    x, y,
//                    0, 0,
//                    width, height,
//                    width, textureHeight);
//        }
//    }

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

    private ResourceLocation getTankTexture(FluidStack fluidStack) {
        if (fluidStack.getFluid() == Fluids.LAVA) {
            return LAVA_LIQUID_TEXTURE;
        } else if (fluidStack.has(DataComponents.POTION_CONTENTS)) {
            return POTION_TANK_TEXTURE;
        } else {
            return WATER_LIQUID_TEXTURE;
        }
    }

    private void setFluidColor(FluidStack fluidStack) {
        if (fluidStack.getFluid() == Fluids.LAVA) {
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
        if (infoButton.isHovered()) {
            guiGraphics.renderTooltip(font, Component.translatable("container.blockysiege.info"), pMouseX, pMouseY);
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
            Fluid fluid = fluidStack.getFluid();
            String fluidName = getFluidName(fluid, fluidStack);

            Component tooltip = Component.literal(fluidName + ": " + fluidAmount + "mB");
            guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    private String getFluidName(Fluid fluid, FluidStack fluidStack) {
        if (fluid == Fluids.LAVA) {
            return "Lava";
        } else if (fluidStack.has(DataComponents.POTION_CONTENTS)) {
            PotionContents potionContents = fluidStack.get(DataComponents.POTION_CONTENTS);
            if (potionContents != null && potionContents.potion().isPresent()) {
                return "Potion";
            }
        }
        return "Water";
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Center the title
        int titleX = (imageWidth - font.width(title)) / 2;
        guiGraphics.drawString(font, title, titleX, titleLabelY, 4210752, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
    }
}