package net.luckystudios;

import net.luckystudios.blocks.custom.shooting.spewer.SpewerCannonModel;
import net.luckystudios.entity.custom.block_projectile.BlockProjectileRenderer;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerCannonRenderer;
import net.luckystudios.blocks.custom.shooting.volley.VolleyBlockRenderer;
import net.luckystudios.blocks.custom.shooting.volley.VolleyRackModel;
import net.luckystudios.entity.custom.cannonball.TrailModel;
import net.luckystudios.entity.custom.new_boats.SloopModel;
import net.luckystudios.entity.custom.new_boats.SloopRenderer;
import net.luckystudios.entity.custom.potion_blob.PotionBlobRenderer;
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
import net.luckystudios.gui.volley.VolleyRackBlockScreen;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Loader-independent client registration. Each loader passes in its own way of registering things.
 */
public class BlockySiegeClient {

    public interface EntityRendererRegistrar {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }

    public interface BlockEntityRendererRegistrar {
        <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider);
    }

    /** Same shape as vanilla's MenuScreens.ScreenConstructor, which is not public in vanilla. */
    @FunctionalInterface
    public interface ScreenFactory<M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> {
        U create(M menu, Inventory inventory, Component title);
    }

    public interface MenuScreenRegistrar {
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ScreenFactory<M, U> factory);
    }

    public interface ParticleProviderRegistrar {
        <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> factory);
    }

    public static void registerLayerDefinitions(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> event) {
        event.accept(ModModelLayers.CANNON, CannonModel::createBodyLayer);
        event.accept(ModModelLayers.MULTI_CANNON, MultiCannonModel::createBodyLayer);
        event.accept(ModModelLayers.SPEWER_CANNON, SpewerCannonModel::createBodyLayer);
        event.accept(ModModelLayers.VOLLEY_RACK, VolleyRackModel::createBodyLayer);
        event.accept(ModModelLayers.CANNON_BALL, CannonBallModel::createBodyLayer);
        event.accept(ModModelLayers.TRAIL, TrailModel::createBodyLayer);
        event.accept(ModModelLayers.WATER_BLOB, WaterBlobModel::createBodyLayer);
        event.accept(ModModelLayers.BALLISTA, BallistaGolemModel::createBodyLayer);
        event.accept(SloopModel.LAYER_LOCATION, SloopModel::createBodyLayer);
    }

    public static void registerBlockEntityRenderers(BlockEntityRendererRegistrar event) {
        event.register(ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), CannonRenderer::new);
        event.register(ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), MultiCannonRenderer::new);
        event.register(ModBlockEntityTypes.SPEWER_BLOCK_ENTITY.get(), SpewerCannonRenderer::new);
        event.register(ModBlockEntityTypes.VOLLEY_BLOCK_ENTITY.get(), VolleyBlockRenderer::new);
    }

    public static void registerEntityRenderers(EntityRendererRegistrar event) {
        event.register(ModEntityTypes.SEAT.get(), NoopRenderer::new);
        event.register(ModEntityTypes.BLOCK_PROJECTILE.get(), BlockProjectileRenderer::new);
        event.register(ModEntityTypes.CANNONBALL.get(), CannonBallRenderer::new);
        event.register(ModEntityTypes.EXPLOSIVE_KEG.get(), ExplosiveKegRenderer::new);
        event.register(ModEntityTypes.FIRE_BOMB.get(), FireBombRenderer::new);
        event.register(ModEntityTypes.FROST_BOMB.get(), FrostBombRenderer::new);
        event.register(ModEntityTypes.WIND_BOMB.get(), WindBombRenderer::new);
        event.register(ModEntityTypes.PRIMED_EXPLOSIVE_BARREL.get(), PrimedExplosiveBarrelRenderer::new);
        event.register(ModEntityTypes.WOODEN_SHRAPNEL.get(), ThrownItemRenderer::new);
        event.register(ModEntityTypes.EMBER.get(), NoopRenderer::new);
        event.register(ModEntityTypes.WATER_BLOB.get(), WaterBlobRenderer::new);
        event.register(ModEntityTypes.POTION_BLOB.get(), PotionBlobRenderer::new);
        event.register(ModEntityTypes.ICE_SHARD.get(), NoopRenderer::new);
        event.register(ModEntityTypes.FIREWORK_STAR.get(), ThrownItemRenderer::new);
        event.register(ModEntityTypes.FALLING_FIREWORK.get(), ThrownItemRenderer::new);
        event.register(ModEntityTypes.BALLISTA.get(), BallistaRenderer::new);
        event.register(ModEntityTypes.SLOOP.get(), SloopRenderer::new);
    }

    public static void registerScreens(MenuScreenRegistrar event) {
        event.register(ModMenuTypes.CANNON_BLOCK_MENU.get(), CannonBlockScreen::new);
        event.register(ModMenuTypes.MULTI_CANNON_BLOCK_MENU.get(), MultiCannonBlockScreen::new);
        event.register(ModMenuTypes.SPEWER_BLOCK_MENU.get(), SpewerCannonBlockScreen::new);
        event.register(ModMenuTypes.VOLLEY_BLOCK_MENU.get(), VolleyRackBlockScreen::new);
        event.register(ModMenuTypes.BALLISTA_BLOCK_MENU.get(), BallistaScreen::new);
    }

    public static void registerParticleFactories(ParticleProviderRegistrar event) {
        event.register(ModParticleTypes.BOTTLE_CAP.get(), BottleCapParticle.Provider::new);
        event.register(ModParticleTypes.FLAME_TRAIL.get(), FlameTrailParticle.Provider::new);
        event.register(ModParticleTypes.WATER_TRAIL.get(), WaterParticle.Provider::new);
    }
}

