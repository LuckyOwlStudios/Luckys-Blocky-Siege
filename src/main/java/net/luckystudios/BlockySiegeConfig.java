package net.luckystudios;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class BlockySiegeConfig {

    // === Spec Builders ===
    public static ModConfigSpec COMMON_CONFIG;
    public static ModConfigSpec CLIENT_CONFIG;

    // === CATEGORY WORLD GENERATION CONFIGURATION ===
    public static String CATEGORY_WORLD_GENERATION = "worldGeneration";
    public static ModConfigSpec.BooleanValue STONE_REPLACERS;
    public static ModConfigSpec.BooleanValue TREMORS;
    public static ModConfigSpec.BooleanValue ROCKS;
    public static ModConfigSpec.BooleanValue GEYSERS;
    public static ModConfigSpec.BooleanValue BOULDERS;

    // === CLIENT CONFIGURATION ===
    public static String CATEGORY_CLIENT = "client";
    public static ModConfigSpec.DoubleValue TREMOR_VOLUME;

    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

        COMMON_BUILDER.comment("World Generation Settings").push(CATEGORY_WORLD_GENERATION);

        STONE_REPLACERS = COMMON_BUILDER
                .comment("The entire overworld stone generation is replaced with a custom stone replacer system.")
                .gameRestart()
                .define("stoneReplacers", false);

        TREMORS = COMMON_BUILDER
                .comment("Whether the tremor event can occur")
                .define("tremors", true);

        ROCKS = COMMON_BUILDER
                .comment("Do rocks generate around the world")
                .define("rocks", true);

        GEYSERS = COMMON_BUILDER
                .comment("Whether the geysers generate around the world")
                .define("geysers", true);

        BOULDERS = COMMON_BUILDER
                .comment("Whether the geysers generate around the world")
                .define("geysers", true);

        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();

        ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

        CLIENT_BUILDER.comment("Client settings").push(CATEGORY_CLIENT);
        TREMOR_VOLUME = CLIENT_BUILDER.comment("Should the hunger bar have a gilded overlay when the player has the Nourishment effect?")
                .defineInRange("tremorVolume", 0.75, 0.0,1.0);
        CLIENT_BUILDER.pop();

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }
}
