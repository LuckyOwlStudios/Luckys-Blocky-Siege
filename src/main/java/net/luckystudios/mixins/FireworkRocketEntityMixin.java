package net.luckystudios.mixins;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin {

    @ModifyVariable(method = "dealExplosionDamage", at = @At("STORE"), ordinal = 0)
    private float modifyExplosionDamage(float originalDamage) {
        // Increase damage by a multiplier (e.g., 2x damage)
        return originalDamage * 3.0f;
    }
}
