package net.luckystudios.events;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.ModEntityTypes;
import net.luckystudios.entity.custom.turrets.ballista.Ballista;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = BlockySiege.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.BALLISTA.get(), Ballista.createAttributes().build());
    }
}
