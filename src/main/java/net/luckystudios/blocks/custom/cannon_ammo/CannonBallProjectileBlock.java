package net.luckystudios.blocks.custom.cannon_ammo;

import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.minecraft.world.level.Level;

public interface CannonBallProjectileBlock {

    AbstractCannonBall asProjectile(Level level, double x, double y, double z);
}
