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
                "Blocky Siege common settings. crewsBringAmmo: whether Pillagers and Piglins reload empty siege weapons "
                        + "with their own ammo. crewSearchRadius: how far (4 to 48 blocks) they look for a weapon to man. "
                        + "Turn crews off entirely with the doSiegeWeaponCrews gamerule.",
                "cannonDamage", String.valueOf(BlockySiegeConfig.CANNON_DAMAGE_DEFAULT),
                "crewsBringAmmo", String.valueOf(BlockySiegeConfig.SIEGE_CREWS_BRING_AMMO_DEFAULT),
                "crewSearchRadius", String.valueOf(BlockySiegeConfig.SIEGE_CREW_SEARCH_RADIUS_DEFAULT));
        float cannonDamage = parseFloat(properties.getProperty("cannonDamage"), BlockySiegeConfig.CANNON_DAMAGE_DEFAULT);
        BlockySiegeConfig.CANNON_DAMAGE = () -> cannonDamage;
        boolean bringAmmo = Boolean.parseBoolean(properties.getProperty("crewsBringAmmo", String.valueOf(BlockySiegeConfig.SIEGE_CREWS_BRING_AMMO_DEFAULT)).trim());
        BlockySiegeConfig.SIEGE_CREWS_BRING_AMMO = () -> bringAmmo;
        int searchRadius = Math.max(4, Math.min(48, (int) parseDouble(properties.getProperty("crewSearchRadius"), BlockySiegeConfig.SIEGE_CREW_SEARCH_RADIUS_DEFAULT)));
        BlockySiegeConfig.SIEGE_CREW_SEARCH_RADIUS = () -> searchRadius;
    }

    public static void loadClient() {
        Properties properties = load("client",
                "Blocky Siege client settings. cannonVolume goes from 0.0 to 1.0",
                "cannonVolume", String.valueOf(BlockySiegeConfig.CANNON_VOLUME_DEFAULT));
        double volume = parseDouble(properties.getProperty("cannonVolume"), BlockySiegeConfig.CANNON_VOLUME_DEFAULT);
        double clamped = Math.max(0.0, Math.min(1.0, volume));
        BlockySiegeConfig.CANNON_VOLUME = () -> clamped;
    }

    /** Reads a properties file, writing any missing keys with their defaults. {@code defaults} is key, value, key, value... */
    private static Properties load(String type, String comment, String... defaults) {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(BlockySiege.MOD_ID + "-" + type + ".properties");
        Properties properties = new Properties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            } catch (IOException e) {
                BlockySiege.LOGGER.error("Could not read {}, using defaults", path, e);
            }
        }
        boolean missing = false;
        for (int i = 0; i + 1 < defaults.length; i += 2) {
            if (!properties.containsKey(defaults[i])) {
                properties.setProperty(defaults[i], defaults[i + 1]);
                missing = true;
            }
        }
        if (missing) {
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
