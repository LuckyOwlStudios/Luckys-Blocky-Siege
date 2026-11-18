package net.luckystudios.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Every packet the mod sends. Each loader registers all of them as bidirectional play packets.
 */
public final class ModNetworking {

    public record PacketType<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type,
                                                           StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                           BiConsumer<T, PacketContext> handler) {
        public void handle(T payload, PacketContext context) {
            handler.accept(payload, context);
        }
    }

    public static final List<PacketType<?>> PACKETS = List.of(
            new PacketType<>(ControllingShootingBlockPacket.TYPE, ControllingShootingBlockPacket.STREAM_CODEC, ControllingShootingBlockPacket::handleData),
            new PacketType<>(CannonBlockScreenPacket.TYPE, CannonBlockScreenPacket.STREAM_CODEC, CannonBlockScreenPacket::handleData),
            new PacketType<>(MultiCannonBlockScreenPacket.TYPE, MultiCannonBlockScreenPacket.STREAM_CODEC, MultiCannonBlockScreenPacket::handleData),
            new PacketType<>(SpewerCannonBlockScreenPacket.TYPE, SpewerCannonBlockScreenPacket.STREAM_CODEC, SpewerCannonBlockScreenPacket::handleData),
            new PacketType<>(VolleyRackBlockScreenPacket.TYPE, VolleyRackBlockScreenPacket.STREAM_CODEC, VolleyRackBlockScreenPacket::handleData)
    );

    private ModNetworking() {
    }
}
