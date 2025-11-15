package net.luckystudios.blocks.util.interfaces;

import net.luckystudios.blocks.util.ModBlockStateProperties;
import net.luckystudios.blocks.util.enums.DamageState;
import net.luckystudios.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

// Add this interface to a block to allow it to be repaired! Though the block MUST have the DamageState blockstate property added to it!
// There is an event that handles the right click functionality already
public interface DamageableBlock {

    Item repairItem();

    default void repairBlock(@Nullable Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        playSound(SoundEvents.ANVIL_USE, player, level, pos);
        level.setBlock(pos, state.setValue(ModBlockStateProperties.DAMAGE_STATE, state.getValue(ModBlockStateProperties.DAMAGE_STATE).repair()), 3);
    }

    default void damageBlock(@Nullable Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        playSound(SoundEvents.METAL_BREAK, player, level, pos);
        level.levelEvent(2001, pos, Block.getId(ModBlocks.CANNON.get().defaultBlockState()));
        if (state.getValue(ModBlockStateProperties.DAMAGE_STATE) == DamageState.HIGH) {
            level.destroyBlock(pos, false);
            playSound(BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.explode")), player, level, pos);
        } else {
            level.setBlock(pos, state.setValue(ModBlockStateProperties.DAMAGE_STATE, state.getValue(ModBlockStateProperties.DAMAGE_STATE).damage()), 3);
        }
    }

    static void playSound(SoundEvent soundEvent, @Nullable Player player, LevelAccessor level, BlockPos pos) {
        Random random = new Random();
        float f1 = 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F;
        level.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 0.3F, f1);
    }
}
