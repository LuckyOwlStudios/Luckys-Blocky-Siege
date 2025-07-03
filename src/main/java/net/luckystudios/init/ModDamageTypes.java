package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;

public interface ModDamageTypes {
    ResourceKey<DamageType> CANNON = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "cannon"));
    ResourceKey<DamageType> BULLET = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "bullet"));

    static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(CANNON, new DamageType("cannon", DamageScaling.NEVER, 0.0F, DamageEffects.HURT, DeathMessageType.DEFAULT));
        context.register(BULLET, new DamageType("bullet", DamageScaling.NEVER, 0.0F, DamageEffects.HURT, DeathMessageType.DEFAULT));
    }
}