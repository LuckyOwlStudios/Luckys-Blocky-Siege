package net.luckystudios.datagen.types;

import net.luckystudios.BlockySiege;
import net.luckystudios.blocks.ModBlocks;
import net.luckystudios.blocks.custom.AbstractCannonBallBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BlockySiege.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        cannonBall(ModBlocks.CANNON_BALL.get(), mcLoc("block/iron_block"));
        cannonBall(ModBlocks.KEG_OF_GUNPOWDER.get(), mcLoc("block/barrel_side"));
        cannonBall(ModBlocks.BLUNDERBOMB.get(), mcLoc("block/glass"));
        cannonBall(ModBlocks.FROST_BOMB.get(), mcLoc("block/ice"));
    }

    private void cannonBall(Block block, ResourceLocation breakParticle) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        ModelFile ballOne = models().withExistingParent(name + "_one", modLoc("block/template_cannon_ball_one")).texture("all", modLoc("block/" + name)).texture("particle", breakParticle);
        ModelFile ballTwo = models().withExistingParent(name + "_two", modLoc("block/template_cannon_ball_two")).texture("all", modLoc("block/" + name)).texture("particle", breakParticle);
        ModelFile ballThree = models().withExistingParent(name + "_three", modLoc("block/template_cannon_ball_three")).texture("all", modLoc("block/" + name)).texture("particle", breakParticle);
        ModelFile ballFour = models().withExistingParent(name + "_four", modLoc("block/template_cannon_ball_four")).texture("all", modLoc("block/" + name)).texture("particle", breakParticle);

        simpleBlockItem(block, ballOne);

        getVariantBuilder(block).forAllStatesExcept(state -> {
            Direction facing = state.getValue(ButtonBlock.FACING);
            int stack = state.getValue(AbstractCannonBallBlock.STACK);

            return ConfiguredModel.builder()
                    .modelFile(switch (stack) {
                        case 1 -> ballOne;
                        case 2 -> ballTwo;
                        case 3 -> ballThree;
                        case 4 -> ballFour;
                        default -> throw new IllegalStateException("Unexpected value: " + stack);
                    })
                    .rotationY((int) (facing.getOpposite()).toYRot())
                    .build();
        }, BlockStateProperties.WATERLOGGED);
    }
}
