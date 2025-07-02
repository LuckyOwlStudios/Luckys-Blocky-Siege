package net.luckystudios.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.seat.Seat;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class CannonOverlay {
    public static void renderCannonOverlay(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // Only render in first person
        if (player == null || mc.options.getCameraType() != CameraType.FIRST_PERSON) return;
        if (!(player.getVehicle() instanceof Seat)) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc(); // <-- Important for transparency
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f); // Alpha = 1 (fully visible, allow PNG alpha)

        guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/gui/sprites/hud/cannon/sight.png"),
                screenWidth / 2 - 8, screenHeight / 2 - 8,
                0.0F, 0.0F,
                15, 15,
                15, 15
        );

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
