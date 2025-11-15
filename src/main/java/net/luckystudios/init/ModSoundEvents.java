package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, BlockySiege.MOD_ID);

    public static final Supplier<SoundEvent> CANNON_FIRE = registerSoundEvent("cannon_fire");
    public static final Supplier<SoundEvent> SHOOT = registerSoundEvent("shoot");
    public static final Supplier<SoundEvent> METAL_HIT = registerSoundEvent("hit_metal");
    public static final Supplier<SoundEvent> BOTTLE_POP_SMALL = registerSoundEvent("bottle_pop_small");
    public static final Supplier<SoundEvent> BOTTLE_POP_LARGE = registerSoundEvent("bottle_pop_large");
    public static final Supplier<SoundEvent> IMPACT_FIERY = registerSoundEvent("impact_fiery");
    public static final Supplier<SoundEvent> IMPACT_ICY = registerSoundEvent("impact_icy");
    public static final Supplier<SoundEvent> FIERY_LOOP = registerSoundEvent("fiery_loop");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
