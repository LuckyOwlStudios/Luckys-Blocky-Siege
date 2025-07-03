package net.luckystudios;

import net.luckystudios.blocks.custom.cannon.types.spewer.SpewerCannonModel;
import net.luckystudios.blocks.custom.cannon.types.spewer.SpewerCannonRenderer;
import net.luckystudios.gui.cannons.ShootingBlockScreen;
import net.luckystudios.blocks.custom.cannon.types.generic.CannonModel;
import net.luckystudios.blocks.custom.cannon.types.multi.MultiCannonModel;
import net.luckystudios.blocks.custom.cannon.types.multi.MultiCannonRenderer;
import net.luckystudios.blocks.custom.iron_gate.IronGateRenderer;
import net.luckystudios.blocks.util.ModBlockEntityTypes;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.blocks.custom.cannon.types.generic.CannonRenderer;
import net.luckystudios.entity.custom.cannon_ball.CannonBallModel;
import net.luckystudios.entity.custom.cannon_ball.explosive_barrel.ExplosiveKegRenderer;
import net.luckystudios.entity.custom.cannon_ball.fire_bomb.FireBombRenderer;
import net.luckystudios.entity.custom.cannon_ball.frost_bomb.FrostBombRenderer;
import net.luckystudios.entity.custom.cannon_ball.normal.CannonBallRenderer;
import net.luckystudios.entity.custom.cannon_ball.wind_bomb.WindBombRenderer;
import net.luckystudios.entity.custom.explosive_barrel.PrimedExplosiveBarrelRenderer;
import net.luckystudios.gui.spewer_cannon.SpewerCannonBlockScreen;
import net.luckystudios.overlays.CannonOverlay;
import net.luckystudios.particles.BottleCapParticle;
import net.luckystudios.particles.CannonFireParticle;
import net.luckystudios.init.ModParticleTypes;
import net.luckystudios.init.ModMenuTypes;
import net.luckystudios.util.ModModelLayers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;

// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = BlockySiege.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BlockySiegeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(CannonOverlay::renderCannonOverlay);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.CANNON, CannonModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MULTI_CANNON, MultiCannonModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SPEWER_CANNON, SpewerCannonModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.IRON_GATE, IronGateRenderer::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.CANNON_BALL, CannonBallModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {

        // Blocks
        event.registerBlockEntityRenderer(ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), CannonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), MultiCannonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SPEWER_CANNON_BLOCK_ENTITY.get(), SpewerCannonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.IRON_GATE.get(), IronGateRenderer::new);

        // Entities
        event.registerEntityRenderer(ModEntityTypes.SEAT.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CANNON_BALL.get(), CannonBallRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.EXPLOSIVE_KEG.get(), ExplosiveKegRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIRE_BOMB.get(), FireBombRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FROST_BOMB.get(), FrostBombRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.WIND_BOMB.get(), WindBombRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.PRIMED_EXPLOSIVE_BARREL.get(), PrimedExplosiveBarrelRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.WOODEN_SHRAPNEL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BULLET.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.CANNON_BLOCK_MENU.get(), ShootingBlockScreen::new);
        event.register(ModMenuTypes.SPEWER_BLOCK_MENU.get(), SpewerCannonBlockScreen::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.BOTTLE_CAP.get(), BottleCapParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CANNON_FIRE.get(), CannonFireParticle.Provider::new);
    }
}
