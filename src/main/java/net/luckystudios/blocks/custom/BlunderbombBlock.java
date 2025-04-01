package net.luckystudios.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.ToIntFunction;

public class BlunderbombBlock extends AbstractCannonBallBlock {

    public static final ToIntFunction<BlockState> LIGHT_EMISSION = state -> 2 * state.getValue(STACK);


    public BlunderbombBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends AbstractCannonBallBlock> codec() {
        return null;
    }
}
