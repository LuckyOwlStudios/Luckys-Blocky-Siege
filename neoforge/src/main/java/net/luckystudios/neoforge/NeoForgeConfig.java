package net.luckystudios.neoforge;

import net.luckystudios.BlockySiegeConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * NeoForge config specs. These keep the same file names and keys as before the multiloader split.
 */
public class NeoForgeConfig {

    public static final ModConfigSpec COMMON_CONFIG;
    public static final ModConfigSpec CLIENT_CONFIG;

    public static final String CATEGORY_WORLD_GENERATION = "worldGeneration";
    public static final ModConfigSpec.ConfigValue<Float> CANNON_DAMAGE;

    public static final String CATEGORY_SIEGE_CREWS = "siegeCrews";
    public static final ModConfigSpec.BooleanValue SIEGE_CREWS_BRING_AMMO;
    public static final ModConfigSpec.IntValue SIEGE_CREW_SEARCH_RADIUS;

    public static final String CATEGORY_CLIENT = "client";
    public static final ModConfigSpec.DoubleValue CANNON_VOLUME;

    static {
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        commonBuilder.comment("Server settings").push(CATEGORY_WORLD_GENERATION);
        CANNON_DAMAGE = commonBuilder
                .comment("How much damage cannon's deal.")
                .define("cannonDamage", BlockySiegeConfig.CANNON_DAMAGE_DEFAULT);
        commonBuilder.pop();
        commonBuilder.comment("Pillagers and Piglins manning cannons, burst cannons and spewers. Turn the whole feature off with the doSiegeWeaponCrews gamerule.").push(CATEGORY_SIEGE_CREWS);
        SIEGE_CREWS_BRING_AMMO = commonBuilder
                .comment("Whether crews reload empty weapons with their own ammo. When false they only use weapons that are already loaded.")
                .define("crewsBringAmmo", BlockySiegeConfig.SIEGE_CREWS_BRING_AMMO_DEFAULT);
        SIEGE_CREW_SEARCH_RADIUS = commonBuilder
                .comment("How far, in blocks, a crew mob with a target looks for a weapon to man.")
                .defineInRange("crewSearchRadius", BlockySiegeConfig.SIEGE_CREW_SEARCH_RADIUS_DEFAULT, 4, 48);
        commonBuilder.pop();
        COMMON_CONFIG = commonBuilder.build();

        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        clientBuilder.comment("Client settings").push(CATEGORY_CLIENT);
        CANNON_VOLUME = clientBuilder
                .comment("Volume for cannon sounds.")
                .defineInRange("cannonVolume", BlockySiegeConfig.CANNON_VOLUME_DEFAULT, 0.0, 1.0);
        clientBuilder.pop();
        CLIENT_CONFIG = clientBuilder.build();
    }

    /** Points the common config values at these specs. */
    public static void bind() {
        BlockySiegeConfig.CANNON_DAMAGE = CANNON_DAMAGE::get;
        BlockySiegeConfig.SIEGE_CREWS_BRING_AMMO = SIEGE_CREWS_BRING_AMMO::get;
        BlockySiegeConfig.SIEGE_CREW_SEARCH_RADIUS = SIEGE_CREW_SEARCH_RADIUS::get;
        BlockySiegeConfig.CANNON_VOLUME = CANNON_VOLUME::get;
    }
}
