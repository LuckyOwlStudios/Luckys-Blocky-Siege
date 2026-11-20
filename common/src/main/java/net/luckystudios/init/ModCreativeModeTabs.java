package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.Services;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final RegistryObject<CreativeModeTab> BLOCKYSIEGE_CREATIVE_TAB = Services.REGISTRY.register(BuiltInRegistries.CREATIVE_MODE_TAB, "blockysiege_tab",
            () -> Services.PLATFORM.creativeTabBuilder().icon(() -> new ItemStack(ModBlocks.CANNONBALL.get()))
                    .title(Component.translatable("itemGroup.blockysiege"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.CANNON.get());
                        output.accept(ModBlocks.MULTI_CANNON.get());
                        output.accept(ModBlocks.SPEWER_CANNON.get());
                        output.accept(ModBlocks.VOLLEY_RACK.get());
                        output.accept(ModBlocks.BALLISTA_BLOCK.get());
                        output.accept(Items.GUNPOWDER);
                        output.accept(ModBlocks.CANNONBALL.get());
                        output.accept(ModBlocks.FIRE_BOMB.get());
                        output.accept(ModBlocks.FROST_BOMB.get());
                        output.accept(ModBlocks.WIND_BOMB.get());
                        output.accept(ModBlocks.EXPLOSIVE_BARREL.get());
                        output.accept(Blocks.TNT);
                        output.accept(ModItems.FORT_BANNER_PATTERN.get());
                    }).build());

    public static void init() {
    }
}
