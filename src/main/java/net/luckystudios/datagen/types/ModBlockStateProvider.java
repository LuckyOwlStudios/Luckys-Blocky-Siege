package net.luckystudios.datagen.types;

import net.luckystudios.BlockySiege;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.blocks.custom.cannon_ammo.AbstractCannonBallBlock;
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
        simpleBlockWithItem(ModBlocks.EXPLOSIVE_BARREL.get(), models().cubeBottomTop("explosive_barrel", modLoc("block/explosive_barrel_side"), mcLoc("block/barrel_bottom"), modLoc("block/explosive_barrel_top")));
        simpleBlockWithItem(ModBlocks.EMBER_PILE.get(), models().carpet("ember_pile", mcLoc("block/magma")));
        simpleBlockWithItem(ModBlocks.FROST_PILE.get(), models().carpet("frost_pile", mcLoc("block/powder_snow")));
        generateCannonBallBlockState(ModBlocks.CANNON_BALL.get(), mcLoc("block/iron_block"), "cutout");
        generateCannonBallBlockState(ModBlocks.EXPLOSIVE_KEG.get(), mcLoc("block/barrel_side"), "cutout");
        generateCannonBallBlockState(ModBlocks.FIRE_BOMB.get(), mcLoc("block/glass"), "translucent");
        generateCannonBallBlockState(ModBlocks.FROST_BOMB.get(), mcLoc("block/ice"), "translucent");
        generateCannonBallBlockState(ModBlocks.WIND_BOMB.get(), modLoc("block/projectile/wind_bomb"), "translucent");
    }

    private void generateCannonBallBlockState(Block block, ResourceLocation breakParticle, String renderType) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        ResourceLocation texture = modLoc("block/projectile/" + name);
        ModelFile ballOne = models().withExistingParent(name + "_one", modLoc("block/template_cannon_ball_one")).texture("all", texture).texture("particle", breakParticle).renderType(renderType);
        ModelFile ballTwo = models().withExistingParent(name + "_two", modLoc("block/template_cannon_ball_two")).texture("all", texture).texture("particle", breakParticle).renderType(renderType);
        ModelFile ballThree = models().withExistingParent(name + "_three", modLoc("block/template_cannon_ball_three")).texture("all", texture).texture("particle", breakParticle).renderType(renderType);
        ModelFile ballFour = models().withExistingParent(name + "_four", modLoc("block/template_cannon_ball_four")).texture("all", texture).texture("particle", breakParticle).renderType(renderType);

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
