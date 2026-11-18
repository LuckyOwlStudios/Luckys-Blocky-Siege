package net.luckystudios.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.luckystudios.BlockySiege;
import net.luckystudios.BlockySiegeConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * A small properties-file config for Fabric, holding the same values as the NeoForge config.
 * Files: config/blockysiege-common.properties and config/blockysiege-client.properties.
 */
public final class FabricConfig {
    private FabricConfig() {
    }

    public static void loadCommon() {
        Properties properties = load("common",
                "Blocky Siege common settings",
                "cannonDamage", String.valueOf(BlockySiegeConfig.CANNON_DAMAGE_DEFAULT));
        float cannonDamage = parseFloat(properties.getProperty("cannonDamage"), BlockySiegeConfig.CANNON_DAMAGE_DEFAULT);
        BlockySiegeConfig.CANNON_DAMAGE = () -> cannonDamage;
    }

    public static void loadClient() {
        Properties properties = load("client",
                "Blocky Siege client settings. cannonVolume goes from 0.0 to 1.0",
                "cannonVolume", String.valueOf(BlockySiegeConfig.CANNON_VOLUME_DEFAULT));
        double volume = parseDouble(properties.getProperty("cannonVolume"), BlockySiegeConfig.CANNON_VOLUME_DEFAULT);
        double clamped = Math.max(0.0, Math.min(1.0, volume));
        BlockySiegeConfig.CANNON_VOLUME = () -> clamped;
    }

    private static Properties load(String type, String comment, String key, String defaultValue) {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(BlockySiege.MOD_ID + "-" + type + ".properties");
        Properties properties = new Properties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            } catch (IOException e) {
                BlockySiege.LOGGER.error("Could not read {}, using defaults", path, e);
            }
        }
        if (!properties.containsKey(key)) {
            properties.setProperty(key, defaultValue);
            try {
                Files.createDirectories(path.getParent());
                try (Writer writer = Files.newBufferedWriter(path)) {
                    properties.store(writer, comment);
                }
            } catch (IOException e) {
                BlockySiege.LOGGER.error("Could not write {}", path, e);
            }
        }
        return properties;
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
