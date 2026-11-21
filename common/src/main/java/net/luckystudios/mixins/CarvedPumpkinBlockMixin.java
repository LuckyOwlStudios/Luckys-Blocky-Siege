package net.luckystudios.mixins;

import net.luckystudios.blocks.custom.turret.ballista.BallistaPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Placing a carved pumpkin (by hand or from a dispenser) completes the ballista pattern, like vanilla golems.
 */
@Mixin(CarvedPumpkinBlock.class)
public class CarvedPumpkinBlockMixin {

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void blockysiege$trySpawnBallista(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston, CallbackInfo ci) {
        if (!oldState.is(state.getBlock()) && BallistaPattern.trySpawn(level, pos)) {
            ci.cancel();
        }
    }
}
