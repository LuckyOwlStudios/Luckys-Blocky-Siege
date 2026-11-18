package net.luckystudios.blocks.custom.cannon_ammo.types;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.cannon_ammo.AbstractCannonBallBlock;
import net.luckystudios.entity.custom.cannonball.types.spreading.frost_bomb.FrostBomb;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
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
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new FrostBomb(level, position.x(), position.y(), position.z());
    }
}
