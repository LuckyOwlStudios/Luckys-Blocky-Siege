package net.luckystudios.blocks.custom.cannon_ammo.types;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.cannon_ammo.AbstractCannonBallBlock;
import net.luckystudios.entity.custom.cannonball.types.spectral.SpectralCannonBall;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpectralCannonBallBlock extends AbstractCannonBallBlock {

    public static final MapCodec<SpectralCannonBallBlock> CODEC = simpleCodec(SpectralCannonBallBlock::new);
    public SpectralCannonBallBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SpectralCannonBallBlock> codec() {
        return CODEC;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new SpectralCannonBall(level, position.x(), position.y(), position.z(), false);
    }
}
