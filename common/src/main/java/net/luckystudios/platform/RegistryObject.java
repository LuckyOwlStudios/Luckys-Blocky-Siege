package net.luckystudios.platform;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * A registered (or soon to be registered) object, independent of the mod loader.
 */
public interface RegistryObject<T> extends Supplier<T> {
    ResourceLocation getId();

    @Override
    T get();
}
