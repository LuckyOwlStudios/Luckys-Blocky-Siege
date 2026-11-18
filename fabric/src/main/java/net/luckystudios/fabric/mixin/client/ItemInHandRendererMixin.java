package net.luckystudios.fabric.mixin.client;

import net.luckystudios.events.mounted_weapon_events.MountedCannonClientBusEvents;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops the first-person hands and held items from rendering while controlling a mounted weapon.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true, require = 0)
    private void blockysiege$hideHandsWhileMounted(CallbackInfo ci) {
        if (MountedCannonClientBusEvents.shouldHideHands()) {
            ci.cancel();
        }
    }
}
