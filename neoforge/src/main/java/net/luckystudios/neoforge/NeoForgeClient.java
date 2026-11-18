package net.luckystudios.neoforge;

import net.luckystudios.BlockySiege;
import net.luckystudios.BlockySiegeClient;
import net.luckystudios.events.mounted_weapon_events.MountedCannonClientBusEvents;
import net.luckystudios.keybinds.ModKeyMappings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.function.Function;

/**
 * NeoForge client hooks. All the actual client logic lives in the common project.
 */
public class NeoForgeClient {

    @EventBusSubscriber(modid = BlockySiege.MOD_ID, value = Dist.CLIENT)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            BlockySiegeClient.registerLayerDefinitions(event::registerLayerDefinition);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            BlockySiegeClient.registerBlockEntityRenderers(new BlockySiegeClient.BlockEntityRendererRegistrar() {
                @Override
                public <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
                    event.registerBlockEntityRenderer(type, provider);
                }
            });
            BlockySiegeClient.registerEntityRenderers(new BlockySiegeClient.EntityRendererRegistrar() {
                @Override
                public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                    event.registerEntityRenderer(type, provider);
                }
            });
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            BlockySiegeClient.registerScreens(new BlockySiegeClient.MenuScreenRegistrar() {
                @Override
                public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, BlockySiegeClient.ScreenFactory<M, U> factory) {
                    event.register(type, factory::create);
                }
            });
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            BlockySiegeClient.registerParticleFactories(new BlockySiegeClient.ParticleProviderRegistrar() {
                @Override
                public <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> factory) {
                    event.registerSpriteSet(type, factory::apply);
                }
            });
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            ModKeyMappings.registerKeyMappings(event::register);
        }
    }

    @EventBusSubscriber(modid = BlockySiege.MOD_ID, value = Dist.CLIENT)
    public static class GameBusEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            ModKeyMappings.onClientTickStart();
        }

        // Hide the crosshair, experience and hotbar while controlling a mounted weapon
        @SubscribeEvent
        public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
            if (event.getName() == VanillaGuiLayers.CROSSHAIR ||
                    event.getName() == VanillaGuiLayers.EXPERIENCE_BAR ||
                    event.getName() == VanillaGuiLayers.EXPERIENCE_LEVEL ||
                    event.getName() == VanillaGuiLayers.HOTBAR) {
                if (MountedCannonClientBusEvents.shouldHideVanillaHud()) {
                    event.setCanceled(true);
                }
            }
        }

        // Draw the mounted weapon HUD on top of the rest of the HUD
        @SubscribeEvent
        public static void onRenderGuiPost(RenderGuiEvent.Post event) {
            MountedCannonClientBusEvents.renderHud(event.getGuiGraphics());
        }

        // Stop the arm from rendering
        @SubscribeEvent
        public static void onRenderArm(RenderArmEvent event) {
            if (MountedCannonClientBusEvents.shouldHideHands()) {
                event.setCanceled(true);
            }
        }

        // Stop the held item from rendering
        @SubscribeEvent
        public static void onRenderItemInHand(RenderHandEvent event) {
            if (MountedCannonClientBusEvents.shouldHideHands()) {
                event.setCanceled(true);
            }
        }
    }
}
