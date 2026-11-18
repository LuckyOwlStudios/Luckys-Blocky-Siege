package net.luckystudios.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.block_projectile.behavior.BlockProjectileBehaviorManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.luckystudios.entity.custom.seat.Seat;
import net.luckystudios.networking.ModNetworking;
import net.luckystudios.networking.PacketContext;
import net.luckystudios.professions.ModTrades;
import net.luckystudios.professions.ModVillagerProfessions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockySiegeFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricConfig.loadCommon();
        BlockySiege.init();

        BlockySiege.registerEntityAttributes(FabricDefaultAttributeRegistry::register);
        registerTrades();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return BlockySiege.id("block_projectile_behaviors");
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepProfiler, ProfilerFiller applyProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return BlockProjectileBehaviorManager.INSTANCE.reload(barrier, manager, prepProfiler, applyProfiler, backgroundExecutor, gameExecutor);
            }
        });
        registerNetworking();

        // Players controlling a mounted weapon cannot use items
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (player.getVehicle() instanceof Seat) {
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });
    }

    private static void registerTrades() {
        Map<Integer, List<VillagerTrades.ItemListing>> tradesByLevel = new HashMap<>();
        ModTrades.addSiegeEngineerTrades((level, listing) -> tradesByLevel.computeIfAbsent(level, l -> new ArrayList<>()).add(listing));
        tradesByLevel.forEach((level, listings) ->
                TradeOfferHelper.registerVillagerOffers(ModVillagerProfessions.SIEGE_ENGINEER_PROFESSION.get(), level, factories -> factories.addAll(listings)));
    }

    private static void registerNetworking() {
        for (ModNetworking.PacketType<?> packet : ModNetworking.PACKETS) {
            registerPacket(packet);
        }
    }

    private static <T extends CustomPacketPayload> void registerPacket(ModNetworking.PacketType<T> packet) {
        PayloadTypeRegistry.playC2S().register(packet.type(), packet.codec());
        PayloadTypeRegistry.playS2C().register(packet.type(), packet.codec());
        ServerPlayNetworking.registerGlobalReceiver(packet.type(), (payload, context) -> packet.handle(payload, new PacketContext() {
            @Override
            public Player player() {
                return context.player();
            }

            @Override
            public boolean isServerbound() {
                return true;
            }

            @Override
            public void enqueueWork(Runnable task) {
                context.server().execute(() -> {
                    try {
                        task.run();
                    } catch (Exception e) {
                        disconnect(Component.literal("Server packet error: " + e.getMessage()));
                    }
                });
            }

            @Override
            public void disconnect(Component reason) {
                context.player().connection.disconnect(reason);
            }
        }));
    }
}
