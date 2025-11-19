package net.luckystudios.networking;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerBlockEntity;
import net.luckystudios.entity.custom.seat.Seat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// This packed is used inside of a cannon or similar's GUI. It's used to send updates to the server.
public class SpewerCannonBlockScreenPacket implements CustomPacketPayload {

    public static final Type<SpewerCannonBlockScreenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "spewer_screen_packet"));

    public static final int AIM_BUTTON = 0;

    public static final StreamCodec<RegistryFriendlyByteBuf, SpewerCannonBlockScreenPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, SpewerCannonBlockScreenPacket message) -> {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeInt(message.eventType);
        buffer.writeInt(message.pressed);
    }, (RegistryFriendlyByteBuf buffer) -> new SpewerCannonBlockScreenPacket(buffer.readBlockPos(), buffer.readInt(), buffer.readInt()));

    BlockPos blockPos;
    public final int eventType;
    public final int pressed;

    public SpewerCannonBlockScreenPacket(BlockPos pos, int eventType, int pressedms) {
        this.blockPos = pos;
        this.eventType = eventType;
        this.pressed = pressedms;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final SpewerCannonBlockScreenPacket message, final IPayloadContext context) {
        if (context.flow() == PacketFlow.SERVERBOUND) {
            context.enqueueWork(() -> {
                pressAction(context.player(), message.blockPos, message.eventType, message.pressed);

                // Now send the same update to the client
                PacketDistributor.sendToPlayer((ServerPlayer) context.player(), message);
            }).exceptionally(e -> {
                context.connection().disconnect(Component.literal("Server packet error: " + e.getMessage()));
                return null;
            });
        } else if (context.flow() == PacketFlow.CLIENTBOUND) {
            context.enqueueWork(() -> {
                pressAction(context.player(), message.blockPos, message.eventType, message.pressed);
            });
        }
    }


    // Ran on Server and Client
    public static void pressAction(Player player, BlockPos pos, int type, int pressed) {
        Level level = player.level();

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SpewerBlockEntity)) return;

        switch (type) {
            case AIM_BUTTON -> {
                player.closeContainer();
                Seat seat = new Seat(level, pos);
                level.addFreshEntity(seat);
                player.startRiding(seat);
            }
        }
    }
}
