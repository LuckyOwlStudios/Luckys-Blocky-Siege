package net.luckystudios.fluids;

import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModFluidTypes;
import net.luckystudios.init.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class PotionFluid extends FlowingFluid {
    @Override
    public Fluid getFlowing() {
        return ModFluids.FLOWING_POTION.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.SOURCE_POTION.get();
    }

    @Override
    public boolean canConvertToSource(FluidState state, Level level, BlockPos pos) {
        return false; // Make it like lava - doesn't create infinite sources
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        // Add any special effects when the fluid destroys a block
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4; // How far the fluid flows horizontally
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 1; // How much the fluid level drops per block
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == ModFluids.SOURCE_POTION.get() || fluid == ModFluids.FLOWING_POTION.get();
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.POTION_LIQUID_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public FluidType getFluidType() {
        return ModFluidTypes.POTION_FLUID_TYPE.get();
    }

    public static class Flowing extends PotionFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public Item getBucket() {
            return Items.LAVA_BUCKET;
        }

        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
            return false;
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 0;
        }

        @Override
        protected float getExplosionResistance() {
            return 0;
        }

        @Override
        protected BlockState createLegacyBlock(FluidState state) {
            return ModBlocks.POTION_LIQUID_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }

        @Override
        protected boolean canConvertToSource(Level level) {
            return false;
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }
    }

    public static class Source extends PotionFluid {
        @Override
        public Item getBucket() {
            return Items.LAVA_BUCKET;
        }

        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
            return false;
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 0;
        }

        @Override
        protected float getExplosionResistance() {
            return 0;
        }

        @Override
        protected BlockState createLegacyBlock(FluidState state) {
            return ModBlocks.POTION_LIQUID_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        protected boolean canConvertToSource(Level level) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }
}
