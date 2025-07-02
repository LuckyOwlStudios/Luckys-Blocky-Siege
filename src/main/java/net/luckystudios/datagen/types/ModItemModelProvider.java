package net.luckystudios.datagen.types;

import net.luckystudios.BlockySiege;
import net.luckystudios.items.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BlockySiege.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.basicItem(ModItems.WOODEN_SHRAPNEL.asItem());
        this.basicItem(ModItems.BULLET.asItem());
    }
}
