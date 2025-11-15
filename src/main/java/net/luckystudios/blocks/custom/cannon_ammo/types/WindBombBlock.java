package net.luckystudios.blocks.custom.cannon_ammo.types;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.cannon_ammo.AbstractCannonBallBlock;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.types.spreading.frost_bomb.FrostBomb;
import net.luckystudios.entity.custom.cannon_ball.types.wind_bomb.WindBomb;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
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
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new WindBomb(level, position.x(), position.y(), position.z());
    }
}
