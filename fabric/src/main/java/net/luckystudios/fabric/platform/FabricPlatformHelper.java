package net.luckystudios.fabric.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.luckystudios.fluids.FluidStack;
import net.luckystudios.fluids.FluidTank;
import net.luckystudios.fluids.PotionFluid;
import net.luckystudios.fluids.VanillaFluidContainers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.luckystudios.platform.services.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory) {
        return new ExtendedScreenHandlerType<>(factory::create, BlockPos.STREAM_CODEC);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
            @Override
            public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) {
                return pos;
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return provider.createMenu(containerId, inventory, menuPlayer);
            }
        });
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return FabricItemGroup.builder();
    }

    @Override
    public FlowingFluid createPotionSourceFluid() {
        return new PotionFluid.Source();
    }

    @Override
    public FlowingFluid createPotionFlowingFluid() {
        return new PotionFluid.Flowing();
    }

    @Override
    public boolean onProjectileImpact(Projectile projectile, HitResult hitResult) {
        // Fabric has no projectile impact event to fire.
        return false;
    }

    @Override
    public <T extends CustomPacketPayload> void sendToServer(T payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public boolean isFluidContainer(ItemStack stack) {
        return VanillaFluidContainers.isContainer(stack);
    }

    @Override
    public @Nullable ItemStack emptyContainerInto(ItemStack container, FluidTank tank) {
        return VanillaFluidContainers.emptyInto(container, tank);
    }

    @Override
    public @Nullable ItemStack fillContainerFrom(ItemStack container, FluidTank tank) {
        return VanillaFluidContainers.fillFrom(container, tank);
    }

    @Override
    public SoundEvent getFluidSound(FluidStack fluid, boolean emptyingIntoTank) {
        return VanillaFluidContainers.sound(fluid, emptyingIntoTank);
    }

    @Override
    public TextureAtlasSprite getFluidStillSprite(FluidStack fluid) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                .getParticleIcon(fluid.getFluid().defaultFluidState().createLegacyBlock());
    }

    @Override
    public int getFluidTint(FluidStack fluid) {
        // Water's texture is grey and is tinted by biome; use the standard water colour. Everything else is untinted.
        return fluid.getFluid() == Fluids.WATER ? 0xFF3F76E4 : 0xFFFFFFFF;
    }
}
