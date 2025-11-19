package net.luckystudios.init;

import com.mojang.serialization.MapCodec;
import net.luckystudios.BlockySiege;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, BlockySiege.MOD_ID);

    public static final Supplier<SimpleParticleType> BOTTLE_CAP =
            PARTICLE_TYPES.register("bottle_cap", () -> new SimpleParticleType(false));

    public static final Supplier<SimpleParticleType> FLAME_TRAIL =
            PARTICLE_TYPES.register("flame_trail", () -> new SimpleParticleType(true));

    public static final Supplier<ParticleType<ColorParticleOption>> WATER_TRAIL =
            PARTICLE_TYPES.register("water_trail", (resourceLocation) ->
                    new ParticleType<ColorParticleOption>(true) {
                        @Override
                        public MapCodec<ColorParticleOption> codec() {
                            return ColorParticleOption.codec(this);
                        }

                        @Override
                        public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                            return ColorParticleOption.streamCodec(this);
                        }
                    }
            );

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
