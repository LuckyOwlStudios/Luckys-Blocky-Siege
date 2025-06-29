package net.luckystudios.blocks.custom.cannon_ammo;

import com.mojang.serialization.MapCodec;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.fire_bomb.FireBomb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.ToIntFunction;

public class FireBombBlock extends AbstractCannonBallBlock {

    public static final MapCodec<FireBombBlock> CODEC = simpleCodec(FireBombBlock::new);
    public static final ToIntFunction<BlockState> LIGHT_EMISSION = state -> 2 * state.getValue(STACK);

    public FireBombBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends AbstractCannonBallBlock> codec() {
        return CODEC;
    }

    @Override
    public AbstractCannonBall asProjectile(Level level, double x, double y, double z) {
        return new FireBomb(level, x, y, z);
    }
}
