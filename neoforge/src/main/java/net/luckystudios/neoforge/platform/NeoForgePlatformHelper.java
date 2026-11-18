package net.luckystudios.neoforge.platform;

import net.luckystudios.fluids.FluidStack;
import net.luckystudios.fluids.FluidTank;
import net.luckystudios.neoforge.NeoForgeFluidHandler;
import net.luckystudios.neoforge.NeoForgePotionFluid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;
import net.luckystudios.platform.services.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory) {
        return IMenuTypeExtension.create((containerId, inventory, buffer) -> factory.create(containerId, inventory, buffer.readBlockPos()));
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(provider, pos);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public FlowingFluid createPotionSourceFluid() {
        return new NeoForgePotionFluid.Source();
    }

    @Override
    public FlowingFluid createPotionFlowingFluid() {
        return new NeoForgePotionFluid.Flowing();
    }

    @Override
    public boolean onProjectileImpact(Projectile projectile, HitResult hitResult) {
        return EventHooks.onProjectileImpact(projectile, hitResult);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToServer(T payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public boolean isFluidContainer(ItemStack stack) {
        return stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    @Override
    public @Nullable ItemStack emptyContainerInto(ItemStack container, FluidTank tank) {
        NeoForgeFluidHandler handler = new NeoForgeFluidHandler(tank);
        FluidActionResult result = FluidUtil.tryEmptyContainer(container, handler, tank.getCapacity(), null, true);
        return result.isSuccess() ? result.getResult() : null;
    }

    @Override
    public @Nullable ItemStack fillContainerFrom(ItemStack container, FluidTank tank) {
        NeoForgeFluidHandler handler = new NeoForgeFluidHandler(tank);
        FluidActionResult result = FluidUtil.tryFillContainer(container, handler, tank.getCapacity(), null, true);
        return result.isSuccess() ? result.getResult() : null;
    }

    @Override
    public SoundEvent getFluidSound(FluidStack fluid, boolean emptyingIntoTank) {
        net.neoforged.neoforge.fluids.FluidStack neoStack = NeoForgeFluidHandler.toNeo(fluid);
        SoundEvent sound = neoStack.getFluidType().getSound(neoStack,
                emptyingIntoTank ? SoundActions.BUCKET_EMPTY : SoundActions.BUCKET_FILL);
        return sound == null ? SoundEvents.BUCKET_FILL : sound;
    }

    @Override
    public TextureAtlasSprite getFluidStillSprite(FluidStack fluid) {
        net.neoforged.neoforge.fluids.FluidStack neoStack = NeoForgeFluidHandler.toNeo(fluid);
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ext.getStillTexture(neoStack));
    }

    @Override
    public int getFluidTint(FluidStack fluid) {
        return IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(NeoForgeFluidHandler.toNeo(fluid));
    }
}
