package net.luckystudios.blocks.custom.cannon.types.spewer;

import com.mojang.serialization.MapCodec;
import net.luckystudios.blocks.custom.cannon.AbstractAimableBlock;
import net.luckystudios.blocks.util.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SpewerCannon extends AbstractAimableBlock {
    public static final MapCodec<SpewerCannon> CODEC = simpleCodec(SpewerCannon::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public SpewerCannon(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpewerCannonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.SPEWER_CANNON_BLOCK_ENTITY.get(), SpewerCannonBlockEntity::tick);
    }

    @Override
    public void triggerBlock(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof SpewerCannonBlockEntity cannonBlockEntity)) return;
        level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
        cannonBlockEntity.cooldown = cannonBlockEntity.maxCooldown; // Set fuse time for the cannon
        cannonBlockEntity.setChanged();
    }
}
