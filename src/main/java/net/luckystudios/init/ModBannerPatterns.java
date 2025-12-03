package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;

public class ModBannerPatterns {

    public static final ResourceKey<BannerPattern> FORT = ResourceKey.create(Registries.BANNER_PATTERN, ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "fort"));

    public static void bootstrap(BootstrapContext<BannerPattern> context) {
        // Register the banner pattern
        // The string parameter is the pattern name that will be used in the texture files
        context.register(FORT, new BannerPattern(BlockySiege.id("fort"), "block.minecraft.banner." + FORT.location().toShortLanguageKey()));
    }
}
