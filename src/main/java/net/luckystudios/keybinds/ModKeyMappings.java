package net.luckystudios.keybinds;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import net.luckystudios.blocks.custom.cannon.AbstractShootingAimableBlockEntity;
import net.luckystudios.entity.custom.seat.Seat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ModKeyMappings {
    public static final Supplier<KeyMapping> FIRE_CANNON = Suppliers.memoize( () -> new KeyMapping(
            "key.blockysiege.fire_cannon",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            "key.categories.gameplay"
    ));

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FIRE_CANNON.get());
    }

    @EventBusSubscriber({Dist.CLIENT})
    public static class KeyEventListener {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
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

            if (inventoryKey.consumeClick()) {
                PacketDistributor.sendToServer(new ControllingCannonPacket(seatPos, 0, 0));
            } else if (fireCannonKey.consumeClick()) {
                PacketDistributor.sendToServer(new ControllingCannonPacket(seatPos, 3, 0));
            }
        }


        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {

        }
    }
}
