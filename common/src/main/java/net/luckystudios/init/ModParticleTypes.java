package net.luckystudios.init;

import com.mojang.serialization.MapCodec;
import net.luckystudios.BlockySiege;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.Services;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final Supplier<SimpleParticleType> BOTTLE_CAP =
            register("bottle_cap", () -> new SimpleParticleType(false));

    public static final Supplier<SimpleParticleType> FLAME_TRAIL =
            register("flame_trail", () -> new SimpleParticleType(true));

    public static final Supplier<ParticleType<ColorParticleOption>> WATER_TRAIL =
            register("water_trail", () ->
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

    private static <T extends ParticleType<?>> RegistryObject<T> register(String name, Supplier<T> factory) {
        return Services.REGISTRY.register(BuiltInRegistries.PARTICLE_TYPE, name, factory);
    }

    public static void init() {
    }
}
