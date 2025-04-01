package net.luckystudios.blocks.custom;

import com.mojang.serialization.MapCodec;

public class KegOfGunpowderBlock extends AbstractCannonBallBlock {
    public static final MapCodec<KegOfGunpowderBlock> CODEC = simpleCodec(KegOfGunpowderBlock::new);

    public KegOfGunpowderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<KegOfGunpowderBlock> codec() {
        return CODEC;
    }
}
