package net.luckystudios.datagen.types;

import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.init.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {

    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        super.addTags(provider);

        tag(ModTags.STRONG_PROJECTILE)
                .add(EntityType.TRIDENT)
                .add(ModEntityTypes.FIREWORK_STAR.get())
        ;
    }
}
