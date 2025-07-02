package net.luckystudios.keybinds;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class TestModKeyMappings {
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

    public static String getKeyName(Supplier<KeyMapping> keyMappingSupplier) {
        KeyMapping keyMapping = keyMappingSupplier.get();
        return keyMapping.getKey().getDisplayName().getString();
    }

    @EventBusSubscriber({Dist.CLIENT})
    public static class KeyEventListener {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            BlockPos blockPos = mc.player.blockPosition();
            if (mc.options.keyInventory.isDown()) {
                if (playerIsControllingCannon(mc.player)) {
                    PacketDistributor.sendToServer(new ControllingCannonPacket(blockPos,0, 0));
                }
            }
        }

        private static boolean playerIsControllingCannon(Player player) {
            // Your logic here – e.g. wearing a helmet, riding a block, etc.
            return true;
        }
    }
}
