package net.luckystudios.neoforge;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.block_projectile.behavior.BlockProjectileBehaviorManager;
import net.luckystudios.networking.ModNetworking;
import net.luckystudios.networking.PacketContext;
import net.luckystudios.neoforge.platform.NeoForgeRegistryHelper;
import net.luckystudios.professions.ModTrades;
import net.luckystudios.professions.ModVillagerProfessions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(BlockySiege.MOD_ID)
public class BlockySiegeNeoForge {

    public BlockySiegeNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        BlockySiege.init();
        NeoForgeRegistryHelper.registerAll(modEventBus);
        ModFluidTypes.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfig.COMMON_CONFIG);
        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.CLIENT_CONFIG);
        NeoForgeConfig.bind();

        modEventBus.addListener(BlockySiegeNeoForge::registerNetworking);
        modEventBus.addListener(BlockySiegeNeoForge::registerAttributes);
        NeoForge.EVENT_BUS.addListener(BlockySiegeNeoForge::addTrades);
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(BlockProjectileBehaviorManager.INSTANCE));
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        BlockySiege.registerEntityAttributes(event::put);
    }

    private static void addTrades(VillagerTradesEvent event) {
        if (event.getType() == ModVillagerProfessions.SIEGE_ENGINEER_PROFESSION.get()) {
            ModTrades.addSiegeEngineerTrades((level, listing) -> event.getTrades().get(level.intValue()).add(listing));
        }
    }

    private static void registerNetworking(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(BlockySiege.MOD_ID);
        for (ModNetworking.PacketType<?> packet : ModNetworking.PACKETS) {
            registerPacket(registrar, packet);
        }
    }

    private static <T extends CustomPacketPayload> void registerPacket(PayloadRegistrar registrar, ModNetworking.PacketType<T> packet) {
        registrar.playBidirectional(packet.type(), packet.codec(), (payload, context) -> packet.handle(payload, wrap(context)));
    }

    private static PacketContext wrap(IPayloadContext context) {
        return new PacketContext() {
            @Override
            public Player player() {
                return context.player();
            }

            @Override
            public boolean isServerbound() {
                return context.flow() == PacketFlow.SERVERBOUND;
            }

            @Override
            public void enqueueWork(Runnable task) {
                context.enqueueWork(task).exceptionally(e -> {
                    if (isServerbound()) {
                        disconnect(Component.literal("Server packet error: " + e.getMessage()));
                    }
                    return null;
                });
            }

            @Override
            public void disconnect(Component reason) {
                context.connection().disconnect(reason);
            }
        };
    }
}
