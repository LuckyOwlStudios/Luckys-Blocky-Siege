package net.luckystudios.blocks.custom.cannon_ammo;

import com.mojang.serialization.MapCodec;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.types.spreading.frost_bomb.FrostBomb;
import net.minecraft.world.level.Level;

public class FrostBombBlock extends AbstractCannonBallBlock {

    public static final MapCodec<FrostBombBlock> CODEC = simpleCodec(FrostBombBlock::new);
    public FrostBombBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FrostBombBlock> codec() {
        return CODEC;
    }

    @Override
    public AbstractCannonBall asProjectile(Level level, double x, double y, double z) {
        return new FrostBomb(level, x, y, z);
    }
}
