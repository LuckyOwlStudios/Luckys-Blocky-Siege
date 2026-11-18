package net.luckystudios.keybinds;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.custom.shooting.spewer.SpewerBlockEntity;
import net.luckystudios.entity.custom.seat.Seat;
import net.luckystudios.networking.ControllingShootingBlockPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.luckystudios.platform.Services;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Key mappings and the client tick logic for controlling a mounted weapon. Each loader registers the key
 * mappings and calls {@link #onClientTickStart()} at the start of every client tick.
 */
public class ModKeyMappings {
    public static final Supplier<KeyMapping> FIRE_CANNON = Suppliers.memoize( () -> new KeyMapping(
            "key.blockysiege.fire_cannon",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            "key.categories.gameplay"
    ));

    public static void registerKeyMappings(Consumer<KeyMapping> registrar) {
        registrar.accept(FIRE_CANNON.get());
    }

    public static void onClientTickStart() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof Seat seat)) return;

        BlockPos seatPos = seat.blockPosition();
        Level level = player.level();

        BlockEntity blockEntity = level.getBlockEntity(seatPos);
        if (!(blockEntity instanceof AbstractShootingAimableBlockEntity)) return;

        KeyMapping inventoryKey = mc.options.keyInventory;
        KeyMapping fireCannonKey = mc.options.keyUse;

        if (blockEntity instanceof SpewerBlockEntity) {
            // The spewer fires continuously while the key is held, at the same rate as a redstone signal
            if (inventoryKey.consumeClick()) {
                Services.PLATFORM.sendToServer(new ControllingShootingBlockPacket(seatPos, 0, 0));
            }
            while (fireCannonKey.consumeClick()) {
                // Clear queued clicks so they don't trigger a block interaction
            }
            if (fireCannonKey.isDown()) {
                Services.PLATFORM.sendToServer(new ControllingShootingBlockPacket(seatPos, ControllingShootingBlockPacket.HOLD_FIRE, 0));
            }
            return;
        }

        if (inventoryKey.consumeClick()) {
            Services.PLATFORM.sendToServer(new ControllingShootingBlockPacket(seatPos, 0, 0));
        } else if (fireCannonKey.consumeClick()) {
            Services.PLATFORM.sendToServer(new ControllingShootingBlockPacket(seatPos, 3, 0));
        }
    }

}
