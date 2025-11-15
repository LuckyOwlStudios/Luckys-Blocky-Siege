package net.luckystudios.events.mounted_weapon_events;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.seat.Seat;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

@EventBusSubscriber(modid = BlockySiege.MOD_ID)
public class MountedCannonEventBusEvents {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();

        // Check if player is mounted (riding an entity)
        if (player.getVehicle() instanceof Seat) {
            // Cancel the item usage
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onUseItem(UseItemOnBlockEvent event) {
//        Player player = event.getPlayer();
//
//        // Check if player is mounted (riding an entity)
//        if (player.getVehicle() instanceof Seat) {
//            // Cancel the item usage
//            event.setCancellationResult(ItemInteractionResult.FAIL);
//        }
    }

    @SubscribeEvent
    public static void onItem(LivingEntityUseItemEvent.Start event) {
        Entity player = event.getEntity();

        // Check if player is mounted (riding an entity)
        if (player.getVehicle() instanceof Seat) {
            // Cancel the item usage
            event.setCanceled(true);
        }
    }
}
