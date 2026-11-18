package net.luckystudios.entity.custom.block_projectile.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.block_projectile.BlockProjectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** The block explodes on impact, like TNT. */
public record Explode(float power, boolean fire) implements BlockProjectileBehavior {

    public static final MapCodec<Explode> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("power").forGetter(Explode::power),
            Codec.BOOL.optionalFieldOf("fire", false).forGetter(Explode::fire)
    ).apply(instance, Explode::new));

    @Override
    public ResourceLocation typeId() {
        return BlockySiege.id("explode");
    }

    @Override
    public void onImpact(ServerLevel level, Vec3 pos, BlockProjectile projectile) {
        level.explode(projectile, pos.x, pos.y, pos.z, power, fire, Level.ExplosionInteraction.TNT);
    }
}
