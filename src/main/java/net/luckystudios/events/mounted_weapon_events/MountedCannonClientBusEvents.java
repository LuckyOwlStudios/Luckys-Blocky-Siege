package net.luckystudios.events.mounted_weapon_events;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.blocks.custom.shooting.cannon.CannonBlockEntity;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerCannonBlockEntity;
import net.luckystudios.blocks.util.ModBlockStateProperties;
import net.luckystudios.entity.custom.seat.Seat;
import net.luckystudios.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

import java.awt.*;

@EventBusSubscriber(modid = BlockySiege.MOD_ID, value = Dist.CLIENT)
public class MountedCannonClientBusEvents {

    // Disable the crosshair when we are controlling a cannon
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Pre event) {
        if (event.getName() == VanillaGuiLayers.CROSSHAIR ||
                event.getName() == VanillaGuiLayers.EXPERIENCE_BAR ||
                event.getName() == VanillaGuiLayers.EXPERIENCE_LEVEL ||
                event.getName() == VanillaGuiLayers.HOTBAR
        ) {
            if (Minecraft.getInstance().player.getVehicle() instanceof Seat) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = player.level();

        if (!isPlayerControllingCannon(player, mc.level)) {
            return;
        }

        Seat seat = (Seat) player.getVehicle();
        BlockPos pos = seat.blockPosition();
        BlockState blockState = mc.level.getBlockState(pos);
        AbstractShootingAimableBlockEntity abstractShootingAimableBlockEntity = (AbstractShootingAimableBlockEntity) level.getBlockEntity(pos);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        renderCrosshair(guiGraphics, blockState, centerX, centerY);
        renderHealthBar(guiGraphics, blockState, centerX, screenHeight);
        renderCenterNumber(guiGraphics, abstractShootingAimableBlockEntity, centerX, screenHeight);
        if (abstractShootingAimableBlockEntity instanceof SpewerCannonBlockEntity) return;
        renderFiringBar(guiGraphics, abstractShootingAimableBlockEntity, centerX, screenHeight);
        renderHotBar(guiGraphics, abstractShootingAimableBlockEntity, centerX, screenHeight);
    }

    private static boolean isPlayerControllingCannon(Player player, Level level) {
        return player != null &&
                level != null &&
                player.getVehicle() instanceof Seat seat &&
                level.getBlockState(seat.blockPosition()).getBlock() instanceof AbstractShootingBlock;
    }

    private static void renderCrosshair(GuiGraphics guiGraphics, BlockState blockState, int centerX, int centerY) {
        boolean isSpewer = blockState.getBlock() == ModBlocks.SPEWER_CANNON.get();

        ResourceLocation crosshairTexture = isSpewer
                ? BlockySiege.id("textures/gui/sprites/hud/cannon/spewer_sight.png")
                : BlockySiege.id("textures/gui/sprites/hud/cannon/cannon_sight.png");

        int crosshairSize = isSpewer ? 30 : 15;
        int crosshairWidth = 15;

        guiGraphics.blit(crosshairTexture,
                centerX - 8, centerY - 8,
                0.0F, 0.0F,
                crosshairWidth, crosshairSize,
                crosshairWidth, crosshairSize);
    }

    private static void renderHealthBar(GuiGraphics guiGraphics, BlockState blockState, int centerX, int screenHeight) {
        if (!blockState.hasProperty(ModBlockStateProperties.DAMAGE_STATE)) {
            return;
        }

        int healthBarX = centerX + 10;
        int healthBarY = screenHeight - 39;

        // Render health bar background
        ResourceLocation healthBarBg = BlockySiege.id("textures/gui/sprites/hud/cannon/health_container.png");
        guiGraphics.blit(healthBarBg, healthBarX, healthBarY, 0.0F, 0.0F, 81, 9, 81, 9);

        // Calculate health level and render health icons
        int healthLevel = getHealthLevel(blockState);
        renderHealthIcons(guiGraphics, healthBarX, healthBarY, healthLevel);
    }

    private static int getHealthLevel(BlockState blockState) {
        return switch (blockState.getValue(ModBlockStateProperties.DAMAGE_STATE)) {
            case NONE -> 4;
            case LOW -> 3;
            case MEDIUM -> 2;
            case HIGH -> 1;
        };
    }

    private static void renderHealthIcons(GuiGraphics guiGraphics, int startX, int startY, int healthLevel) {
        ResourceLocation healthIcon = BlockySiege.id("textures/gui/sprites/hud/cannon/health_icon.png");
        ResourceLocation damageIcon = BlockySiege.id("textures/gui/sprites/hud/cannon/damage_icon.png");

        int iconWidth = 19;
        int iconHeight = 7;
        int iconSpacing = 20;
        int iconOffsetX = 1;
        int iconOffsetY = 1;

        for (int i = 0; i < 4; i++) {
            ResourceLocation icon = (i < healthLevel) ? healthIcon : damageIcon;
            int iconX = startX + iconOffsetX + (i * iconSpacing);
            int iconY = startY + iconOffsetY;

            guiGraphics.blit(icon, iconX, iconY, 0.0F, 0.0F, iconWidth, iconHeight, iconWidth, iconHeight);
        }
    }

    private static void renderCenterNumber(GuiGraphics guiGraphics, AbstractShootingAimableBlockEntity abstractShootingAimableBlockEntity, int centerX, int screenHeight) {
        int number;
        if (abstractShootingAimableBlockEntity instanceof CannonBlockEntity cannonBlockEntity) {
            number = cannonBlockEntity.firePower;
        } else {
            return;
        }
        String numberText = String.valueOf(number);

        // Get the font from Minecraft
        Font font = Minecraft.getInstance().font;

        // Calculate position (same as experience bar number)
        int textWidth = font.width(numberText);
        int textX = centerX - (textWidth / 2);
        int textY = screenHeight - 40; // Experience bar is at -31, text is 4 pixels above

        // Render the number with shadow (like experience bar)
        guiGraphics.drawString(font, numberText, textX, textY, Color.GRAY.getRGB(), true); // Green color with shadow
    }

    private static void renderFiringBar(GuiGraphics guiGraphics, AbstractShootingAimableBlockEntity abstractShootingAimableBlockEntity, int centerX, int screenHeight) {
        ResourceLocation firingBar = BlockySiege.id("textures/gui/sprites/hud/cannon/firing_bar.png");
        ResourceLocation firingBarProgress = BlockySiege.id("textures/gui/sprites/hud/cannon/firing_bar_progress.png");
        int xPos = centerX - 91;
        int textY = screenHeight - 29;
        guiGraphics.blit(firingBar, xPos, textY, 0.0F, 0.0F, 182, 5, 182, 5);
    }

    private static void renderHotBar(GuiGraphics guiGraphics, AbstractShootingAimableBlockEntity abstractShootingAimableBlockEntity, int centerX, int screenHeight) {
        ResourceLocation firingBar = BlockySiege.id("textures/gui/sprites/hud/cannon/cannon_hotbar.png");
        int xPos = centerX - 91;
        int yPos = screenHeight - 22;
        guiGraphics.blit(firingBar, xPos, yPos, 0.0F, 0.0F, 182, 22, 182, 22);

        ItemStack fuseStack = abstractShootingAimableBlockEntity.getItem(0);
        ItemStack ammoStack = abstractShootingAimableBlockEntity.getItem(1);

        Font font = Minecraft.getInstance().font;

        // Render fuse item
        int fuseX = centerX + 32;
        int fuseY = yPos + 3;
        guiGraphics.renderItem(fuseStack, fuseX, fuseY);
        guiGraphics.renderItemDecorations(font, fuseStack, fuseX, fuseY);

        // Render ammo item
        int ammoX = centerX - 48;
        int ammoY = yPos + 3;
        guiGraphics.renderItem(ammoStack, ammoX, ammoY);
        guiGraphics.renderItemDecorations(font, ammoStack, ammoX, ammoY);
    }

    // Stop the arm from rendering
    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        Player player = Minecraft.getInstance().player;

        // Check if the player is mounted
        if (player != null && player.getVehicle() instanceof Seat) {
            // Cancel the arm rendering
            event.setCanceled(true);
        }
    }

    // Stop the arm from rendering
    @SubscribeEvent
    public static void onRenderItemInHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;

        if (player != null && player.getVehicle() instanceof Seat) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (event.getEntity() instanceof Player player && event.isMounting()) {
            Entity mountedEntity = event.getEntityBeingMounted();

            if (mountedEntity instanceof Seat) {
                // Set custom pose when mounting
                player.setPose(Pose.SLEEPING);
            }
        } else if (event.getEntity() instanceof Player player && !event.isMounting()) {
            // Reset pose when dismounting
            player.setPose(Pose.STANDING);
        }
    }
}
