package net.luckystudios;

import java.util.function.Supplier;

/**
 * The mod's config values. Each loader reads its own config file and replaces these suppliers on startup;
 * until then they return the defaults.
 */
public class BlockySiegeConfig {

    // === COMMON CONFIGURATION ===
    public static final float CANNON_DAMAGE_DEFAULT = 25F;
    /** How much damage cannons deal. */
    public static Supplier<Float> CANNON_DAMAGE = () -> CANNON_DAMAGE_DEFAULT;

    public static final boolean SIEGE_CREWS_BRING_AMMO_DEFAULT = true;
    /** Whether Pillagers and Piglins reload empty siege weapons with their own ammo, instead of only using loaded ones. */
    public static Supplier<Boolean> SIEGE_CREWS_BRING_AMMO = () -> SIEGE_CREWS_BRING_AMMO_DEFAULT;

    public static final int SIEGE_CREW_SEARCH_RADIUS_DEFAULT = 16;
    /** How far, in blocks, Pillagers and Piglins look for a siege weapon to man when they have a target. */
    public static Supplier<Integer> SIEGE_CREW_SEARCH_RADIUS = () -> SIEGE_CREW_SEARCH_RADIUS_DEFAULT;

    // === CLIENT CONFIGURATION ===
    public static final double CANNON_VOLUME_DEFAULT = 1.0;
    /** Volume for cannon sounds, from 0 to 1. */
    public static Supplier<Double> CANNON_VOLUME = () -> CANNON_VOLUME_DEFAULT;
}
