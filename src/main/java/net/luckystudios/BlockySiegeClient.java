package net.luckystudios;

import net.luckystudios.blocks.custom.cannon.inventory.CannonScreen;
import net.luckystudios.blocks.util.ModBlockEntityTypes;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.blocks.custom.cannon.CannonRenderer;
import net.luckystudios.entity.custom.cannon_ball.CannonBallModel;
import net.luckystudios.entity.custom.cannon_ball.explosive_barrel.ExplosiveKegRenderer;
import net.luckystudios.entity.custom.cannon_ball.fire_bomb.FireBombRenderer;
import net.luckystudios.entity.custom.cannon_ball.frost_bomb.FrostBombRenderer;
import net.luckystudios.entity.custom.cannon_ball.normal.CannonBallRenderer;
import net.luckystudios.entity.custom.cannon_ball.wind_bomb.WindBombRenderer;
import net.luckystudios.entity.custom.explosive_barrel.PrimedExplosiveBarrelRenderer;
import net.luckystudios.screens.ModMenuTypes;
import net.luckystudios.util.ModModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = BlockySiege.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BlockySiegeClient
{
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntityTypes.SEAT.get(), NoopRenderer::new);
        EntityRenderers.register(ModEntityTypes.CANNON_BALL.get(), CannonBallRenderer::new);
        EntityRenderers.register(ModEntityTypes.EXPLOSIVE_KEG.get(), ExplosiveKegRenderer::new);
        EntityRenderers.register(ModEntityTypes.FIRE_BOMB.get(), FireBombRenderer::new);
        EntityRenderers.register(ModEntityTypes.FROST_BOMB.get(), FrostBombRenderer::new);
        EntityRenderers.register(ModEntityTypes.WIND_BOMB.get(), WindBombRenderer::new);
        EntityRenderers.register(ModEntityTypes.PRIMED_EXPLOSIVE_BARREL.get(), PrimedExplosiveBarrelRenderer::new);
        EntityRenderers.register(ModEntityTypes.WOODEN_SHRAPNEL.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.CANNON, CannonRenderer::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.CANNON_BALL, CannonBallModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), CannonRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.CANNON_BLOCK_MENU.get(), CannonScreen::new);
    }
}
