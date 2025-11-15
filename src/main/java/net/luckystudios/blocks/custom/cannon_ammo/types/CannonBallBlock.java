package net.luckystudios.blocks.custom.cannon_ammo.types;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.cannon_ammo.AbstractCannonBallBlock;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.types.normal.CannonBall;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
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
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new CannonBall(level, position.x(), position.y(), position.z());
    }
}
