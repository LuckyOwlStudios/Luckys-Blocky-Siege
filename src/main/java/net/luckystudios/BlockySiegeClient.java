package net.luckystudios;

import net.luckystudios.blocks.custom.shooting.spewer.SpewerCannonModel;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerCannonRenderer;
import net.luckystudios.entity.custom.cannonball.TrailModel;
import net.luckystudios.entity.custom.turrets.ballista.BallistaGolemModel;
import net.luckystudios.entity.custom.turrets.ballista.BallistaRenderer;
import net.luckystudios.entity.custom.water_drop.WaterBlobModel;
import net.luckystudios.entity.custom.water_drop.WaterBlobRenderer;
import net.luckystudios.gui.ballista.BallistaScreen;
import net.luckystudios.gui.cannons.CannonBlockScreen;
import net.luckystudios.blocks.custom.shooting.cannon.CannonModel;
import net.luckystudios.blocks.custom.shooting.multi_cannon.MultiCannonModel;
import net.luckystudios.blocks.custom.shooting.multi_cannon.MultiCannonRenderer;
import net.luckystudios.gui.multi_cannon.MultiCannonBlockScreen;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.blocks.custom.shooting.cannon.CannonRenderer;
import net.luckystudios.entity.custom.cannonball.CannonBallModel;
import net.luckystudios.entity.custom.cannonball.types.explosive_barrel.ExplosiveKegRenderer;
import net.luckystudios.entity.custom.cannonball.types.spreading.fire_bomb.FireBombRenderer;
import net.luckystudios.entity.custom.cannonball.types.spreading.frost_bomb.FrostBombRenderer;
import net.luckystudios.entity.custom.cannonball.types.normal.CannonBallRenderer;
import net.luckystudios.entity.custom.cannonball.types.wind_bomb.WindBombRenderer;
import net.luckystudios.entity.custom.explosive_barrel.PrimedExplosiveBarrelRenderer;
import net.luckystudios.gui.spewer_cannon.SpewerCannonBlockScreen;
import net.luckystudios.particles.BottleCapParticle;
import net.luckystudios.init.ModParticleTypes;
import net.luckystudios.init.ModMenuTypes;
import net.luckystudios.particles.FlameTrailParticle;
import net.luckystudios.particles.WaterParticle;
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

// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = BlockySiege.MOD_ID, value = Dist.CLIENT)
public class BlockySiegeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.CANNON, CannonModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MULTI_CANNON, MultiCannonModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SPEWER_CANNON, SpewerCannonModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.CANNON_BALL, CannonBallModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.TRAIL, TrailModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.WATER_BLOB, WaterBlobModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.BALLISTA, BallistaGolemModel::createBodyLayer);
    }

    // Registering entity renderers!
    @SubscribeEvent
    public static void registerER(EntityRenderersEvent.RegisterRenderers event) {

        // Blocks
        event.registerBlockEntityRenderer(ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), CannonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), MultiCannonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SPEWER_CANNON_BLOCK_ENTITY.get(), SpewerCannonRenderer::new);

        // Entities
        event.registerEntityRenderer(ModEntityTypes.SEAT.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CANNONBALL.get(), CannonBallRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.EXPLOSIVE_KEG.get(), ExplosiveKegRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIRE_BOMB.get(), FireBombRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FROST_BOMB.get(), FrostBombRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.WIND_BOMB.get(), WindBombRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.PRIMED_EXPLOSIVE_BARREL.get(), PrimedExplosiveBarrelRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.WOODEN_SHRAPNEL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.EMBER.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.WATER_BLOB.get(), WaterBlobRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ICE_SHARD.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIREWORK_STAR.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BALLISTA.get(), BallistaRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.CANNON_BLOCK_MENU.get(), CannonBlockScreen::new);
        event.register(ModMenuTypes.MULTI_CANNON_BLOCK_MENU.get(), MultiCannonBlockScreen::new);
        event.register(ModMenuTypes.SPEWER_BLOCK_MENU.get(), SpewerCannonBlockScreen::new);
        event.register(ModMenuTypes.BALLISTA_BLOCK_MENU.get(), BallistaScreen::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.BOTTLE_CAP.get(), BottleCapParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLAME_TRAIL.get(), FlameTrailParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.WATER_TRAIL.get(), WaterParticle.Provider::new);
    }
}
