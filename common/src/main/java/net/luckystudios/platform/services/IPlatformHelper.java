package net.luckystudios.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.HitResult;

public interface IPlatformHelper {

    /** The name of the running loader, e.g. "NeoForge" or "Fabric". */
    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    // ---- Menus ----

    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory inventory, BlockPos pos);
    }

    /** Creates a menu type whose client side receives the block position of the block that opened it. */
    <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory);

    /** Opens a menu created by {@link #createMenuType} and sends {@code pos} to the client. */
    void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos);

    // ---- Registration helpers ----

    CreativeModeTab.Builder creativeTabBuilder();

    FlowingFluid createPotionSourceFluid();

    FlowingFluid createPotionFlowingFluid();

    // ---- Hooks ----

    /** Fires the loader's projectile impact event, if it has one. Returns true when the impact was cancelled. */
    boolean onProjectileImpact(Projectile projectile, HitResult hitResult);

    // ---- Networking ----

    <T extends CustomPacketPayload> void sendToServer(T payload);

    <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T payload);
}
