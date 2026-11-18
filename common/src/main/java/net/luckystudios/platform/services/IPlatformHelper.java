package net.luckystudios.platform.services;

import net.luckystudios.fluids.FluidStack;
import net.luckystudios.fluids.FluidTank;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
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

    // ---- Fluids ----

    /** True for items that can hold fluid, such as buckets and modded tanks (empty or full). */
    boolean isFluidContainer(ItemStack stack);

    /** Pours the container's fluid into the tank. Returns the leftover container, or null when nothing could be moved. */
    @Nullable
    ItemStack emptyContainerInto(ItemStack container, FluidTank tank);

    /** Fills the container from the tank. Returns the filled container, or null when nothing could be moved. */
    @Nullable
    ItemStack fillContainerFrom(ItemStack container, FluidTank tank);

    SoundEvent getFluidSound(FluidStack fluid, boolean emptyingIntoTank);

    /** The fluid's own still texture, for drawing it in GUIs. Client only. */
    TextureAtlasSprite getFluidStillSprite(FluidStack fluid);

    /** ARGB tint for the fluid's texture. Client only. */
    int getFluidTint(FluidStack fluid);
}
