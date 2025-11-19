package net.luckystudios;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class BlockySiegeConfig {

    // === Spec Builders ===
    public static ModConfigSpec COMMON_CONFIG;
    public static ModConfigSpec CLIENT_CONFIG;
    public static ModConfigSpec SERVER_CONFIG;

    // === CATEGORY WORLD GENERATION CONFIGURATION ===
    public static String CATEGORY_WORLD_GENERATION = "worldGeneration";
    public static ModConfigSpec.ConfigValue<Float> CANNON_DAMAGE;

    // === CLIENT CONFIGURATION ===
    public static String CATEGORY_CLIENT = "client";
    public static ModConfigSpec.DoubleValue CANNON_VOLUME;

    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
        COMMON_BUILDER.comment("Server settings").push(CATEGORY_WORLD_GENERATION);

        CANNON_DAMAGE = COMMON_BUILDER
                .comment("How much damage cannon's deal.")
                .define("cannonDamage", 25F);

        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();

        ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
        CLIENT_BUILDER.comment("Client settings").push(CATEGORY_CLIENT);

        CANNON_VOLUME = CLIENT_BUILDER  // ✅ Use CLIENT_BUILDER
                .comment("Volume for cannon sounds.")
                .defineInRange("cannonVolume", 1.0, 0.0, 1.0);  // Fix name and remove Float.class

        CLIENT_BUILDER.pop();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }
}
