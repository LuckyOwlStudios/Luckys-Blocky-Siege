package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.Services;

import java.util.function.Supplier;

public class ModSoundEvents {
    public static final Supplier<SoundEvent> CANNON_FIRE = registerSoundEvent("cannon_fire");
    public static final Supplier<SoundEvent> CANNON_FIRE_DISTANT = registerSoundEvent("cannon_fire_distant");
    public static final Supplier<SoundEvent> SHOOT = registerSoundEvent("shoot");
    public static final Supplier<SoundEvent> METAL_HIT = registerSoundEvent("hit_metal");
    public static final Supplier<SoundEvent> BOTTLE_POP_SMALL = registerSoundEvent("bottle_pop_small");
    public static final Supplier<SoundEvent> BOTTLE_POP_LARGE = registerSoundEvent("bottle_pop_large");
    public static final Supplier<SoundEvent> IMPACT_FIERY = registerSoundEvent("impact_fiery");
    public static final Supplier<SoundEvent> IMPACT_ICY = registerSoundEvent("impact_icy");
    public static final Supplier<SoundEvent> FIERY_LOOP = registerSoundEvent("fiery_loop");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, name);
        return Services.REGISTRY.register(BuiltInRegistries.SOUND_EVENT, name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
    }
}
