package net.luckystudios.datagen.types;

import net.luckystudios.BlockySiege;
import net.luckystudios.init.ModBannerPatterns;
import net.luckystudios.init.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BannerItemTagProvider extends BannerPatternTagsProvider {
    public BannerItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, BlockySiege.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        super.addTags(provider);

        tag(ModTags.PATTERN_ITEM_FORT).add(ModBannerPatterns.FORT);
    }
}
