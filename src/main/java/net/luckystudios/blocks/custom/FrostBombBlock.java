package net.luckystudios.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FrostBombBlock extends AbstractCannonBallBlock {

    public static final MapCodec<FrostBombBlock> CODEC = simpleCodec(FrostBombBlock::new);
    public FrostBombBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FrostBombBlock> codec() {
        return CODEC;
    }
}
