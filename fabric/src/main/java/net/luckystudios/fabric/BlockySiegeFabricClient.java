package net.luckystudios.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.luckystudios.BlockySiegeClient;
import net.luckystudios.events.mounted_weapon_events.MountedCannonClientBusEvents;
import net.luckystudios.keybinds.ModKeyMappings;
import net.luckystudios.networking.ModNetworking;
import net.luckystudios.networking.PacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

public class BlockySiegeFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricConfig.loadClient();

        BlockySiegeClient.registerLayerDefinitions((layer, definition) ->
                EntityModelLayerRegistry.registerModelLayer(layer, definition::get));

        BlockySiegeClient.registerBlockEntityRenderers(new BlockySiegeClient.BlockEntityRendererRegistrar() {
            @Override
            public <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
                BlockEntityRenderers.register(type, provider);
            }
        });

        BlockySiegeClient.registerEntityRenderers(new BlockySiegeClient.EntityRendererRegistrar() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                EntityRendererRegistry.register(type, provider);
            }
        });

        BlockySiegeClient.registerScreens(new BlockySiegeClient.MenuScreenRegistrar() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, BlockySiegeClient.ScreenFactory<M, U> factory) {
                MenuScreens.register(type, factory::create);
            }
        });

        BlockySiegeClient.registerParticleFactories(new BlockySiegeClient.ParticleProviderRegistrar() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> factory) {
                ParticleFactoryRegistry.getInstance().register(type, factory::apply);
            }
        });

        ModKeyMappings.registerKeyMappings(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.START_CLIENT_TICK.register(client -> ModKeyMappings.onClientTickStart());
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> MountedCannonClientBusEvents.renderHud(guiGraphics));

        for (ModNetworking.PacketType<?> packet : ModNetworking.PACKETS) {
            registerClientReceiver(packet);
        }
    }

    private static <T extends CustomPacketPayload> void registerClientReceiver(ModNetworking.PacketType<T> packet) {
        ClientPlayNetworking.registerGlobalReceiver(packet.type(), (payload, context) -> packet.handle(payload, new PacketContext() {
            @Override
            public Player player() {
                return context.player();
            }

            @Override
            public boolean isServerbound() {
                return false;
            }

            @Override
            public void enqueueWork(Runnable task) {
                context.client().execute(task);
            }

            @Override
            public void disconnect(Component reason) {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if (connection != null) {
                    connection.getConnection().disconnect(reason);
                }
            }
        }));
    }
}
