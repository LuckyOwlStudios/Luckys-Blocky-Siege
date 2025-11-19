package net.luckystudios.events.mounted_weapon_events;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.blocks.custom.shooting.cannon.CannonBlockEntity;
import net.luckystudios.blocks.custom.shooting.multi_cannon.MultiCannonBlockEntity;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerBlockEntity;
import net.luckystudios.blocks.custom.shooting.volley.VolleyRackBlockEntity;
import net.luckystudios.blocks.util.ModBlockStateProperties;
import net.luckystudios.entity.custom.seat.Seat;
import net.luckystudios.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
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

        if (!isPlayerControllingGun(player, mc.level) || mc.options.hideGui) {
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

        if (abstractShootingAimableBlockEntity instanceof CannonBlockEntity cannonBlockEntity) {
            renderCannonHUD(guiGraphics, blockState, centerX, centerY, screenHeight, cannonBlockEntity);
        } else if (abstractShootingAimableBlockEntity instanceof MultiCannonBlockEntity multiCannonBlockEntity) {
            renderMultiCannonHUD(guiGraphics, blockState, centerX, centerY, screenHeight, multiCannonBlockEntity);
        } else if (abstractShootingAimableBlockEntity instanceof SpewerBlockEntity spewerBlockEntity) {
            renderSpewerHUD(guiGraphics, blockState, centerX, centerY, screenHeight, spewerBlockEntity);
        } else if (abstractShootingAimableBlockEntity instanceof VolleyRackBlockEntity volleyRackBlockEntity) {
            renderVolleyRackHUD(guiGraphics, blockState, centerX, centerY, screenHeight, volleyRackBlockEntity);
        }
    }

    private static boolean isPlayerControllingGun(Player player, Level level) {
        return player != null &&
                level != null &&
                player.getVehicle() instanceof Seat seat &&
                level.getBlockState(seat.blockPosition()).getBlock() instanceof AbstractShootingBlock;
    }

    private static void renderCannonHUD(GuiGraphics guiGraphics, BlockState blockState, int centerX, int centerY, int screenHeight, CannonBlockEntity cannonBlockEntity) {
        renderCrosshair(guiGraphics, BlockySiege.id("textures/gui/sprites/hud/firing_hud/sight.png"), 15, 15, centerX, centerY);
        renderHealthBar(guiGraphics, blockState, centerX, screenHeight, 4);
        renderCenterNumber(guiGraphics, cannonBlockEntity, centerX, screenHeight);
        renderHotBar(guiGraphics, cannonBlockEntity, centerX, screenHeight);
        float progress = cannonBlockEntity.getFiringProgress();
        renderFiringBar(guiGraphics, centerX, screenHeight, progress, ResourceLocation.withDefaultNamespace("textures/gui/sprites/boss_bar/white_progress.png"), new Color(1,1 - progress, 1 - progress, 1));
    }

    private static void renderMultiCannonHUD(GuiGraphics guiGraphics, BlockState blockState, int centerX, int centerY, int screenHeight, MultiCannonBlockEntity multiCannonBlockEntity) {
        renderCrosshair(guiGraphics, BlockySiege.id("textures/gui/sprites/hud/firing_hud/sight.png"), 15, 15, centerX, centerY);
        renderHealthBar(guiGraphics, blockState, centerX, screenHeight, 4);
        renderCenterNumber(guiGraphics, multiCannonBlockEntity, centerX, screenHeight);
        renderHotBar(guiGraphics, multiCannonBlockEntity, centerX, screenHeight);
        renderFiringBar(guiGraphics, centerX, screenHeight, 1, ResourceLocation.withDefaultNamespace("textures/gui/sprites/boss_bar/white_progress.png"), Color.WHITE);
    }

    private static void renderSpewerHUD(GuiGraphics guiGraphics, BlockState blockState, int centerX, int centerY, int screenHeight, SpewerBlockEntity spewerBlockEntity) {
        renderCrosshair(guiGraphics, BlockySiege.id("textures/gui/sprites/hud/firing_hud/spewer/spewer_sight.png"), 15, 30, centerX, centerY);
        renderHealthBar(guiGraphics, blockState, centerX, screenHeight, 4);
        ResourceLocation hotbar = BlockySiege.id("textures/gui/sprites/hud/firing_hud/spewer/spewer_hotbar.png");
        int xPos = centerX - 91;
        int yPos = screenHeight - 22;
        guiGraphics.blit(hotbar, xPos, yPos, 0.0F, 0.0F, 182, 22, 182, 22);
        float progress = spewerBlockEntity.getFillPercentage();
        Color color;
        if (spewerBlockEntity.getFluid() == Fluids.LAVA) {
            // Convert float values (0-1) to int values (0-255)
            color = new Color(255, 55, 0, 255);
        } else {
            PotionContents potionContents = spewerBlockEntity.getFluidTank().getFluid().get(DataComponents.POTION_CONTENTS);
            if (potionContents != null) {
                int potionColor = potionContents.getColor();
                // Extract RGB components and create Color with proper constructor
                int red = (potionColor >> 16) & 0xFF;
                int green = (potionColor >> 8) & 0xFF;
                int blue = potionColor & 0xFF;
                color = new Color(red, green, blue, 255);
            } else {
                int waterColor = BiomeColors.getAverageWaterColor(spewerBlockEntity.getLevel(), spewerBlockEntity.getBlockPos());
                color = new Color(waterColor);
            }
        }
        renderFiringBar(guiGraphics, centerX, screenHeight, progress, BlockySiege.id("textures/gui/sprites/hud/firing_hud/spewer/liquid_progress.png"), color);
    }

    private static void renderVolleyRackHUD(GuiGraphics guiGraphics, BlockState blockState, int centerX, int centerY, int screenHeight, VolleyRackBlockEntity volleyRackBlockEntity) {
        renderCrosshair(guiGraphics, BlockySiege.id("textures/gui/sprites/hud/firing_hud/volley_rack/volley_rack_sight.png"), 45, 30, centerX, centerY);
        renderHealthBar(guiGraphics, blockState, centerX, screenHeight, 2);
        ResourceLocation firingBar = BlockySiege.id("textures/gui/sprites/hud/firing_hud/volley_rack/volley_rack_hotbar.png");
        int xPos = centerX - 93;
        int yPos = screenHeight - 22;
        guiGraphics.blit(firingBar, xPos, yPos, 0.0F, 0.0F, 186, 22, 186, 22);

        Font font = Minecraft.getInstance().font;

        // Render Fuse Slot
        int fuseX = xPos + 3;
        int fuseY = yPos + 3;
        ItemStack fuseStack = volleyRackBlockEntity.getItem(0);
        guiGraphics.renderItem(fuseStack, fuseX, fuseY);
        guiGraphics.renderItemDecorations(font, fuseStack, fuseX, fuseY);

        // Render Ammo Slots
        int startX = fuseX + 24;
        for (int i = 1; i <= 8; i++) {
            ItemStack ammoStack = volleyRackBlockEntity.getItem(i);
            int slotX = startX + (i - 1) * 20; // (i - 1) because slot 1 should be at startX + 0
            guiGraphics.renderItem(ammoStack, slotX, fuseY);
            guiGraphics.renderItemDecorations(font, ammoStack, slotX, fuseY);
        }
    }

    private static void renderCrosshair(GuiGraphics guiGraphics, ResourceLocation texture, int crosshairWidth, int crosshairHeight, int centerX, int centerY) {
        guiGraphics.blit(texture,
                centerX - crosshairWidth / 2, centerY - crosshairHeight / 2,
                0.0F, 0.0F,
                crosshairWidth, crosshairHeight,
                crosshairWidth, crosshairHeight);
    }

    private static void renderHealthBar(GuiGraphics guiGraphics, BlockState blockState, int centerX, int screenHeight, int type) {
        if (!blockState.hasProperty(ModBlockStateProperties.DAMAGE_STATE)) {
            return;
        }

        int healthBarX = centerX + 10;
        int healthBarY = screenHeight - 39;

        // Render health bar background
        ResourceLocation healthBarBg = BlockySiege.id("textures/gui/sprites/hud/firing_hud/health_container_" + type + ".png");
        guiGraphics.blit(healthBarBg, healthBarX, healthBarY, 0.0F, 0.0F, 81, 9, 81, 9);

        // Calculate health level and render health icons
        int healthLevel = getHealthLevel(blockState);
        renderHealthBarIcons(guiGraphics, healthBarX, healthBarY, healthLevel, type);
    }

    private static int getHealthLevel(BlockState blockState) {
        return switch (blockState.getValue(ModBlockStateProperties.DAMAGE_STATE)) {
            case NONE -> 4;
            case LOW -> 3;
            case MEDIUM -> 2;
            case HIGH -> 1;
        };
    }

    private static void renderHealthBarIcons(GuiGraphics guiGraphics, int startX, int startY, int healthLevel, int type) {
        ResourceLocation healthIcon = BlockySiege.id("textures/gui/sprites/hud/firing_hud/health_icon_" + type + ".png");
        ResourceLocation damageIcon = BlockySiege.id("textures/gui/sprites/hud/firing_hud/damage_icon_" + type + ".png");

        int iconWidth = type == 4 ? 19 : 39;
        int iconHeight = 7;
        int iconSpacing = type == 4 ? 20 : 40;

        for (int i = 0; i < type; i++) {
            ResourceLocation icon = (i < healthLevel) ? healthIcon : damageIcon;
            int iconX = startX + 1 + (i * iconSpacing);
            int iconY = startY + 1;

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

    private static void renderFiringBar(GuiGraphics guiGraphics, int centerX, int screenHeight, float progress, ResourceLocation firingBarProgress, Color color) {
        ResourceLocation firingBar = BlockySiege.id("textures/gui/sprites/hud/firing_hud/firing_bar.png");
        int xPos = centerX - 91;
        int textY = screenHeight - 29;
        guiGraphics.blit(firingBar, xPos, textY, 0.0F, 0.0F, 182, 5, 182, 5);
        int UVWidth = (int)(182 * progress);

        // Convert integer color values (0-255) to float values (0.0-1.0)
        guiGraphics.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        guiGraphics.blit(firingBarProgress, xPos, textY, 0.0F, 0.0F, UVWidth, 5, 182, 5);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderHotBar(GuiGraphics guiGraphics, AbstractShootingAimableBlockEntity abstractShootingAimableBlockEntity, int centerX, int screenHeight) {
        ResourceLocation hotbar = BlockySiege.id("textures/gui/sprites/hud/firing_hud/firing_hotbar.png");
        int xPos = centerX - 91;
        int yPos = screenHeight - 22;
        guiGraphics.blit(hotbar, xPos, yPos, 0.0F, 0.0F, 182, 22, 182, 22);

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
