package net.luckystudios.platform;

import net.luckystudios.BlockySiege;
import net.luckystudios.platform.services.IPlatformHelper;
import net.luckystudios.platform.services.IRegistryHelper;

import java.util.ServiceLoader;

/**
 * Finds the loader-specific implementation of each service through Java's {@link ServiceLoader}.
 * Each loader lists its implementations in {@code META-INF/services}.
 */
public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        BlockySiege.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
