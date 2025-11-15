package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.luckystudios.gui.ballista.BallistaMenu;
import net.luckystudios.gui.cannons.CannonBlockMenu;
import net.luckystudios.gui.multi_cannon.MultiCannonBlockMenu;
import net.luckystudios.gui.spewer_cannon.SpewerCannonBlockBlockMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, BlockySiege.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CannonBlockMenu>> CANNON_BLOCK_MENU =
            registerMenuType("cannon_block_menu", CannonBlockMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<MultiCannonBlockMenu>> MULTI_CANNON_BLOCK_MENU =
            registerMenuType("multi_cannon_block_menu", MultiCannonBlockMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<SpewerCannonBlockBlockMenu>> SPEWER_BLOCK_MENU =
            registerMenuType("spewer_cannon_block_menu", SpewerCannonBlockBlockMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<BallistaMenu>> BALLISTA_BLOCK_MENU =
            registerMenuType("ballista_block_menu", BallistaMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
