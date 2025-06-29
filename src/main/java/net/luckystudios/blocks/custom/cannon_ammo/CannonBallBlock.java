package net.luckystudios.blocks.custom.cannon_ammo;

import com.mojang.serialization.MapCodec;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.normal.CannonBall;
import net.minecraft.world.level.Level;

public class CannonBallBlock extends AbstractCannonBallBlock {

    public static final MapCodec<CannonBallBlock> CODEC = simpleCodec(CannonBallBlock::new);
    public CannonBallBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<CannonBallBlock> codec() {
        return CODEC;
    }

    @Override
    public AbstractCannonBall asProjectile(Level level, double x, double y, double z) {
        return new CannonBall(level, x, y, z);
    }
}
