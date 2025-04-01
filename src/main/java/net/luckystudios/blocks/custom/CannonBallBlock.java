package net.luckystudios.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CannonBallBlock extends AbstractCannonBallBlock {

    public static final MapCodec<CannonBallBlock> CODEC = simpleCodec(CannonBallBlock::new);
    public CannonBallBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<CannonBallBlock> codec() {
        return CODEC;
    }
}
