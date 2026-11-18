package net.luckystudios.fabric.mixin.client;

import net.luckystudios.events.mounted_weapon_events.MountedCannonClientBusEvents;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the crosshair, experience bar, experience level and hotbar while controlling a mounted weapon,
 * like the NeoForge version does through its GUI layer events.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = {"renderCrosshair", "renderItemHotbar", "renderExperienceBar", "renderExperienceLevel"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void blockysiege$hideWhileMounted(CallbackInfo ci) {
        if (MountedCannonClientBusEvents.shouldHideVanillaHud()) {
            ci.cancel();
        }
    }
}
