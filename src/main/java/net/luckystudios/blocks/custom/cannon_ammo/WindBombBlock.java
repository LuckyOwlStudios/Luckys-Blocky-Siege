package net.luckystudios.blocks.custom.cannon_ammo;

import com.mojang.serialization.MapCodec;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.types.wind_bomb.WindBomb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.ToIntFunction;

public class WindBombBlock extends AbstractCannonBallBlock {

    public static final MapCodec<WindBombBlock> CODEC = simpleCodec(WindBombBlock::new);
    public static final ToIntFunction<BlockState> LIGHT_EMISSION = state -> 2 * state.getValue(STACK);

    public WindBombBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends AbstractCannonBallBlock> codec() {
        return CODEC;
    }

    @Override
    public AbstractCannonBall asProjectile(Level level, double x, double y, double z) {
        return new WindBomb(level, x, y, z);
    }
}
