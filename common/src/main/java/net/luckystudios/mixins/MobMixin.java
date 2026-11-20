package net.luckystudios.mixins;

import net.luckystudios.entity.ai.crew.SiegeCrewAI;
import net.luckystudios.entity.ai.crew.SiegeCrewHolder;
import net.luckystudios.entity.ai.crew.SiegeCrewState;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives every mob a place to keep siege crew state, and lets the crew AI take over while it runs a weapon. */
@Mixin(Mob.class)
public abstract class MobMixin implements SiegeCrewHolder {

    @Unique
    private SiegeCrewState blockysiege$crewState;

    @Override
    public SiegeCrewState blockysiege$getCrewState() {
        if (this.blockysiege$crewState == null) {
            this.blockysiege$crewState = new SiegeCrewState();
        }
        return this.blockysiege$crewState;
    }

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void blockysiege$runSiegeCrew(CallbackInfo ci) {
        if (SiegeCrewAI.tick((Mob) (Object) this)) {
            ci.cancel();
        }
    }
}
