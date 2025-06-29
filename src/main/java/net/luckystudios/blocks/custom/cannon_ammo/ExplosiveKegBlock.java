package net.luckystudios.blocks.custom.cannon_ammo;

import com.mojang.serialization.MapCodec;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.entity.custom.cannon_ball.explosive_barrel.ExplosiveKeg;
import net.minecraft.world.level.Level;

public class ExplosiveKegBlock extends AbstractCannonBallBlock {

    public static final MapCodec<ExplosiveKegBlock> CODEC = simpleCodec(ExplosiveKegBlock::new);

    public ExplosiveKegBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ExplosiveKegBlock> codec() {
        return CODEC;
    }

    @Override
    public AbstractCannonBall asProjectile(Level level, double x, double y, double z) {
        return new ExplosiveKeg(level, x, y, z);
    }
}
