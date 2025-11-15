package net.luckystudios.events;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.shooting.AbstractShootingBlock;
import net.luckystudios.entity.custom.seat.Seat;
import net.luckystudios.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = BlockySiege.MOD_ID, value = Dist.CLIENT)
public class ModClientBusEvents {

    // Disable the crosshair when we are controlling a cannon
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Pre event) {
        if (event.getName() == VanillaGuiLayers.CROSSHAIR) {
            if (Minecraft.getInstance().player.getVehicle() instanceof Seat) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getVehicle() instanceof Seat seat) {
                BlockPos pos = seat.blockPosition();
                BlockState blockState = mc.level.getBlockState(pos);
                if (mc.level != null && blockState.getBlock() instanceof AbstractShootingBlock) {
                    GuiGraphics guiGraphics = event.getGuiGraphics();
                    int screenWidth = guiGraphics.guiWidth();
                    int screenHeight = guiGraphics.guiHeight();

                    // Add some debug logging
                    System.out.println("Rendering cannon sight at: " + (screenWidth / 2) + ", " + (screenHeight / 2));

                    boolean isSpewer = blockState.getBlock() == ModBlocks.SPEWER_CANNON.get();

                    ResourceLocation texture = isSpewer ? BlockySiege.id("textures/gui/sprites/hud/cannon/spewer_sight.png") : BlockySiege.id("textures/gui/sprites/hud/cannon/cannon_sight.png");

                    guiGraphics.blit(
                            texture,
                            screenWidth / 2 - 8, screenHeight / 2 - 8,
                            0.0F, 0.0F,
                            15, isSpewer ? 30 : 15,
                            15,  isSpewer ? 30 : 15
                    );
                }
            }
    }


    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Pre event) {

    }
}
