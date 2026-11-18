package net.luckystudios.professions;

import com.google.common.collect.ImmutableSet;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;

public class ModVillagerProfessions {

    public static final RegistryObject<PoiType> SIEGE_ENGINEER_POI =
            Services.REGISTRY.registerPoi("siege_engineer_poi", ModBlocks.CANNON, 1, 1);

    public static final RegistryObject<VillagerProfession> SIEGE_ENGINEER_PROFESSION = Services.REGISTRY.register(BuiltInRegistries.VILLAGER_PROFESSION, "siege_engineer",
            () -> new VillagerProfession("siege_engineer", poiTypeHolder -> poiTypeHolder.value() == SIEGE_ENGINEER_POI.get(), poiTypeHolder -> poiTypeHolder.value() == SIEGE_ENGINEER_POI.get(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN));

    public static void init() {
    }
}
