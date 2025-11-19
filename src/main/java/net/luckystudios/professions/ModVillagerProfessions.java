package net.luckystudios.professions;

import com.google.common.collect.ImmutableSet;
import net.luckystudios.BlockySiege;
import net.luckystudios.init.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModVillagerProfessions {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BlockySiege.MOD_ID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, BlockySiege.MOD_ID);

    public static final Holder<PoiType> SIEGE_ENGINEER_POI = POI_TYPES.register("siege_engineer_poi",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.CANNON.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final Holder<VillagerProfession> SIEGE_ENGINEER_PROFESSION = VILLAGER_PROFESSIONS.register("siege_engineer",
            () -> new VillagerProfession("siege_engineer", poiTypeHolder -> poiTypeHolder.value() == SIEGE_ENGINEER_POI.value(), poiTypeHolder -> poiTypeHolder.value() == SIEGE_ENGINEER_POI.value(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN));

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }
}
