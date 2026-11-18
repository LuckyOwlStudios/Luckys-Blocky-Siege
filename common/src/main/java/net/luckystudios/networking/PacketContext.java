package net.luckystudios.networking;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * What a packet handler gets to know about the packet, independent of the mod loader.
 */
public interface PacketContext {
    /** The player who sent the packet on the server, or the local player on the client. */
    Player player();

    /** True when the packet travelled from the client to the server. */
    boolean isServerbound();

    /** Runs the task on the main game thread. */
    void enqueueWork(Runnable task);

    void disconnect(Component reason);
}
