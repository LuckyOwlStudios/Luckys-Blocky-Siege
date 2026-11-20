package net.luckystudios.mixins;

import net.luckystudios.entity.ai.crew.SiegeCrewAI;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /** A crew mob that dies at its post gets its held items back first, so they drop as usual. */
    @Inject(method = "die", at = @At("HEAD"))
    private void blockysiege$restoreCrewHands(DamageSource damageSource, CallbackInfo ci) {
        if ((Object) this instanceof Mob mob && !mob.level().isClientSide) {
            SiegeCrewAI.restoreHands(mob);
        }
    }
}
