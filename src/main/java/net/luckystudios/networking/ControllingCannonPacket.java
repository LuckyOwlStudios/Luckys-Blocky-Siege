package net.luckystudios.networking;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.custom.cannon.AbstractAimableBlock;
import net.luckystudios.blocks.custom.cannon.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.custom.cannon.types.generic.CannonBlockEntity;
import net.luckystudios.blocks.custom.cannon.types.multi.MultiCannonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// This packet is used when a player is controlling a AbstractShootingAimableBlock (like a cannon) and sends actions to the server
public class ControllingCannonPacket implements CustomPacketPayload {

    public static final Type<ControllingCannonPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "key_helmet_toggle"));

    public static final int OPEN_INVENTORY = 0;
    public static final int DECREASE_POWER = 1;
    public static final int INCREASE_POWER = 2;
    public static final int FIRE_CANNON = 3;

    public static final StreamCodec<RegistryFriendlyByteBuf, ControllingCannonPacket> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, ControllingCannonPacket message) -> {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeInt(message.eventType);
        buffer.writeInt(message.pressed);
    }, (RegistryFriendlyByteBuf buffer) -> new ControllingCannonPacket(buffer.readBlockPos(), buffer.readInt(), buffer.readInt()));

    BlockPos blockPos;
    public final int eventType;
    public final int pressed;

    public ControllingCannonPacket(BlockPos pos, int eventType, int pressedms) {
        this.blockPos = pos;
        this.eventType = eventType;
        this.pressed = pressedms;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final ControllingCannonPacket message, final IPayloadContext context) {
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

    public static void pressAction(Player player, BlockPos pos, int type, int pressedms) {
        Level level = player.level();

        // Security measure to prevent arbitrary chunk generation
        if (!level.hasChunkAt(player.blockPosition())) return;

        BlockState blockState = level.getBlockState(pos);

        switch (type) {
            case OPEN_INVENTORY -> {
                // Open the cannon inventory GUI
                if (level.getBlockEntity(pos) instanceof CannonBlockEntity cannonBlockEntity) {
                    player.openMenu(new SimpleMenuProvider(cannonBlockEntity, Component.translatable("container.blockysiege.cannon")), pos);
                }
                if (level.getBlockEntity(pos) instanceof MultiCannonBlockEntity multiCannonBlockEntity) {
                    player.openMenu(new SimpleMenuProvider(multiCannonBlockEntity, Component.translatable("container.blockysiege.multi_cannon")), pos);
                }
            }
            case DECREASE_POWER, INCREASE_POWER -> {

            }
            case FIRE_CANNON -> {
                if (!(blockState.getBlock() instanceof AbstractAimableBlock aimableBlockEntity)) return;
                aimableBlockEntity.triggerBlock(blockState, level, pos);
            }
        }
    }
}
