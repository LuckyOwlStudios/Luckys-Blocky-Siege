package net.luckystudios.gui.multi_cannon;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.shooting.multi_cannon.MultiCannonBlockEntity;
import net.luckystudios.networking.CannonBlockScreenPacket;
import net.luckystudios.networking.MultiCannonBlockScreenPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;


// This is the image
public class MultiCannonBlockScreen extends AbstractContainerScreen<MultiCannonBlockMenu> {

    Level level;
    BlockPos blockPos;
    Player player;
    MultiCannonBlockEntity multiCannonBlockEntity;

    ImageButton targetButton;
    ImageButton bulletCount1;
    ImageButton bulletCount2;
    ImageButton bulletCount3;
    ImageButton bulletCount4;

    private static final ResourceLocation GUI_TEXTURE =
            BlockySiege.id("textures/gui/container/cannon_gui.png");

    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
           BlockySiege.id("textures/gui/container/target.png"),
            BlockySiege.id("textures/gui/container/target_highlighted.png")
    );

    private static final ResourceLocation POWER_SLOT_SPRITE = BlockySiege.id("textures/gui/container/power.png");
    private static final ResourceLocation MISSING_SLOT_SPRITE = BlockySiege.id("textures/gui/container/missing_power.png");
    private static final ResourceLocation HIGHLIGHT_POWER_SLOT = BlockySiege.id("textures/gui/container/power_hover.png");
    public MultiCannonBlockScreen(MultiCannonBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.player = playerInventory.player;
        this.level = player.level();
        this.blockPos = menu.blockEntity.getBlockPos();
        this.multiCannonBlockEntity = menu.blockEntity;
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
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        int powerX = this.leftPos + 16;
        int firePower = multiCannonBlockEntity.bullet_count;
        int fuseAmmo = multiCannonBlockEntity.getFuse().getCount();
        guiGraphics.blit(fuseAmmo > 0 ? POWER_SLOT_SPRITE : MISSING_SLOT_SPRITE, powerX, this.topPos + 50, 0, 0, 16, 8, 16, 8);
        if (firePower > 1) {
            guiGraphics.blit(fuseAmmo > 1 ? POWER_SLOT_SPRITE : MISSING_SLOT_SPRITE, powerX, this.topPos + 42, 0, 0, 16, 8, 16, 8);
            if (firePower > 2) {
                guiGraphics.blit(fuseAmmo > 2 ? POWER_SLOT_SPRITE : MISSING_SLOT_SPRITE, powerX, this.topPos + 34, 0, 0, 16, 8, 16, 8);
                if (firePower > 3) {
                    guiGraphics.blit(fuseAmmo > 3 ? POWER_SLOT_SPRITE : MISSING_SLOT_SPRITE, powerX, this.topPos + 26, 0, 0, 16, 8, 16, 8);
                }
            }
        }
        RenderSystem.disableBlend();
    }


    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(guiGraphics, pMouseX, pMouseY);

        // Render tooltip for target button if hovered
        if (targetButton.isHovered()) {
            guiGraphics.renderTooltip(font, Component.translatable("container.blockysiege.aim"), pMouseX, pMouseY);
        }

        if (bulletCount1.isHovered() || bulletCount2.isHovered() || bulletCount3.isHovered() || bulletCount4.isHovered()) guiGraphics.renderTooltip(font, Component.translatable("container.blockysiege.multi_cannon.set_count"), pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Center the title at the top of the GUI
        int titleX = (this.imageWidth - this.font.width(this.title)) / 2;
        guiGraphics.drawString(this.font, this.title, titleX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    protected void init() {
        super.init();

        // Position button relative to the GUI
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Initialize the button
        targetButton = new ImageButton(this.leftPos + 156, this.topPos + 6, 14, 14, BUTTON_SPRITES, button ->
                PacketDistributor.sendToServer(new MultiCannonBlockScreenPacket(blockPos, 0, 0))) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
            }
        };
        // Render the button
        this.addRenderableWidget(targetButton);
        int powerX = this.leftPos + 16;

        bulletCount1 = new ImageButton(powerX, this.topPos + 50, 16, 8, BUTTON_SPRITES, button ->
                PacketDistributor.sendToServer(new MultiCannonBlockScreenPacket(blockPos, 1, 0))) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                if (isHovered()) {
                    guiGraphics.blit(HIGHLIGHT_POWER_SLOT, getX(), getY(), 0, 0, width, height, width, height);
                }
            }
        };
        this.addRenderableWidget(bulletCount1);

        bulletCount2 = new ImageButton(powerX, this.topPos + 42, 16, 8, BUTTON_SPRITES, button ->
                PacketDistributor.sendToServer(new MultiCannonBlockScreenPacket(blockPos, 2, 0))) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                if (isHovered()) {
                    guiGraphics.blit(HIGHLIGHT_POWER_SLOT, getX(), getY(), 0, 0, width, height, width, height);
                }
            }
        };
        this.addRenderableWidget(bulletCount2);

        bulletCount3 = new ImageButton(powerX, this.topPos + 34, 16, 8, BUTTON_SPRITES, button ->
                PacketDistributor.sendToServer(new MultiCannonBlockScreenPacket(blockPos, 3, 0))) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                if (isHovered()) {
                    guiGraphics.blit(HIGHLIGHT_POWER_SLOT, getX(), getY(), 0, 0, width, height, width, height);
                }
            }
        };
        this.addRenderableWidget(bulletCount3);

        bulletCount4 = new ImageButton(powerX, this.topPos + 26, 16, 8, BUTTON_SPRITES, button ->
                PacketDistributor.sendToServer(new MultiCannonBlockScreenPacket(blockPos, 4, 0))) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                if (isHovered()) {
                    guiGraphics.blit(HIGHLIGHT_POWER_SLOT, getX(), getY(), 0, 0, width, height, width, height);
                }
            }
        };
        this.addRenderableWidget(bulletCount4);
    }
}
