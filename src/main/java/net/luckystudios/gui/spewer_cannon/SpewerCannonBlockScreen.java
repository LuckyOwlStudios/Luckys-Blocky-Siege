package net.luckystudios.gui.spewer_cannon;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.cannon.AbstractShootingAimableBlockEntity;
import net.luckystudios.networking.ShooingBlockScreenPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class SpewerCannonBlockScreen extends AbstractContainerScreen<SpewerCannonBlockBlockMenu> {

    Level level;
    BlockPos blockPos;
    Player player;
    AbstractShootingAimableBlockEntity cannonBlockEntity;

    ImageButton targetButton;

    private static final ResourceLocation GUI_TEXTURE =
            BlockySiege.id("textures/gui/container/spewer_cannon_gui.png");

    private static final WidgetSprites AIR_BUTTON_SPRITES = new WidgetSprites(
            BlockySiege.id("textures/gui/container/target.png"),
            BlockySiege.id("textures/gui/container/target_highlighted.png")
    );

    private static final ResourceLocation BUCKET_SLOT_SPRITE = BlockySiege.id("textures/gui/sprites/container/slot/bucket.png");
    private static final ResourceLocation POTION_SLOT_SPRITE = BlockySiege.id("textures/gui/sprites/container/slot/potion.png");
    private static final List<ResourceLocation> EMPTY_SLOT_AMMO_ITEMS;
    private final CyclingSlotBackground ammoIcon = new CyclingSlotBackground(0);

    public SpewerCannonBlockScreen(SpewerCannonBlockBlockMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.player = playerInventory.player;
        this.level = player.level();
        this.blockPos = menu.blockEntity.getBlockPos();
        this.cannonBlockEntity = menu.blockEntity;
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
        this.ammoIcon.render(this.menu, guiGraphics, pPartialTick, this.leftPos, this.topPos);
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.ammoIcon.tick(EMPTY_SLOT_AMMO_ITEMS);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(guiGraphics, pMouseX, pMouseY);

        // Render tooltip for target button if hovered
        if (targetButton.isHovered()) {
            guiGraphics.renderTooltip(font, Component.translatable("container.blockysiege.cannon.aim"), pMouseX, pMouseY);
        }
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
        targetButton = new ImageButton(this.leftPos + 156, this.topPos + 6, 14, 14, AIR_BUTTON_SPRITES, button ->
                PacketDistributor.sendToServer(new ShooingBlockScreenPacket(blockPos, 0, 0))) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
            }
        };
        // Render the button
        this.addRenderableWidget(targetButton);
    }

    static {
        EMPTY_SLOT_AMMO_ITEMS = List.of(BUCKET_SLOT_SPRITE, POTION_SLOT_SPRITE);
    }
}
