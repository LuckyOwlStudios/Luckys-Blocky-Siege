package net.luckystudios.blocks.custom.cannon_ammo.types;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.cannon_ammo.AbstractCannonBallBlock;
import net.luckystudios.entity.custom.cannonball.types.spreading.fire_bomb.FireBomb;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
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
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new FireBomb(level, position.x(), position.y(), position.z());
    }
}
