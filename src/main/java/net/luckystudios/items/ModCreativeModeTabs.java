package net.luckystudios.items;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BlockySiege.MOD_ID);

    public static final Supplier<CreativeModeTab> BLOCKYSIEGE_CREATIVE_TAB = CREATIVE_MODE_TAB.register("blockysiege_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.CANNON_BALL.get()))
                    .title(Component.translatable("itemGroup.blockysiege"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.CANNON_BALL);
                        output.accept(ModBlocks.KEG_OF_GUNPOWDER);
                        output.accept(ModBlocks.BLUNDERBOMB);
                        output.accept(ModBlocks.FROST_BOMB);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
