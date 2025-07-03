package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, BlockySiege.MOD_ID);

    public static final Supplier<SimpleParticleType> BOTTLE_CAP =
            PARTICLE_TYPES.register("bottle_cap", () -> new SimpleParticleType(true));

    public static final Supplier<SimpleParticleType> CANNON_FIRE =
            PARTICLE_TYPES.register("cannon_fire", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
