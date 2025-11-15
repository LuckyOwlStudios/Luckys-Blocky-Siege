package net.luckystudios.events;

import net.luckystudios.BlockySiege;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.entity.custom.turrets.ballista.BallistaEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = BlockySiege.MOD_ID)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.BALLISTA.get(), BallistaEntity.createAttributes().build());
    }
}
