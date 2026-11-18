package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.luckystudios.gui.ballista.BallistaMenu;
import net.luckystudios.gui.cannons.CannonBlockMenu;
import net.luckystudios.gui.multi_cannon.MultiCannonBlockMenu;
import net.luckystudios.gui.spewer_cannon.SpewerCannonBlockBlockMenu;
import net.luckystudios.gui.volley.VolleyRackBlockMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.luckystudios.platform.RegistryObject;
import net.luckystudios.platform.Services;
import net.luckystudios.platform.services.IPlatformHelper;

public class ModMenuTypes {
    public static final RegistryObject<MenuType<CannonBlockMenu>> CANNON_BLOCK_MENU =
            registerMenuType("cannon_block_menu", CannonBlockMenu::new);

    public static final RegistryObject<MenuType<MultiCannonBlockMenu>> MULTI_CANNON_BLOCK_MENU =
            registerMenuType("multi_cannon_block_menu", MultiCannonBlockMenu::new);

    public static final RegistryObject<MenuType<SpewerCannonBlockBlockMenu>> SPEWER_BLOCK_MENU =
            registerMenuType("spewer_cannon_block_menu", SpewerCannonBlockBlockMenu::new);

    public static final RegistryObject<MenuType<VolleyRackBlockMenu>> VOLLEY_BLOCK_MENU =
            registerMenuType("volley_block_menu", VolleyRackBlockMenu::new);

    public static final RegistryObject<MenuType<BallistaMenu>> BALLISTA_BLOCK_MENU =
            registerMenuType("ballista_block_menu", BallistaMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IPlatformHelper.MenuFactory<T> factory) {
        return Services.REGISTRY.register(BuiltInRegistries.MENU, name, () -> Services.PLATFORM.createMenuType(factory));
    }

    public static void init() {
    }
}
