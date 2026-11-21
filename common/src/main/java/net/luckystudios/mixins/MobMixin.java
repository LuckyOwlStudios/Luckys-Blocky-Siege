package net.luckystudios.mixins;

import net.luckystudios.entity.ai.crew.SiegeCrewAI;
import net.luckystudios.entity.ai.crew.SiegeCrewHolder;
import net.luckystudios.entity.ai.crew.SiegeCrewState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
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

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void blockysiege$saveStashedHands(CompoundTag compound, CallbackInfo ci) {
        SiegeCrewState state = this.blockysiege$getCrewState();
        if (state.stashedMainHand.isEmpty() && state.stashedOffHand.isEmpty()) return;
        Mob self = (Mob) (Object) this;
        CompoundTag stash = new CompoundTag();
        if (!state.stashedMainHand.isEmpty()) stash.put("MainHand", state.stashedMainHand.save(self.registryAccess()));
        if (!state.stashedOffHand.isEmpty()) stash.put("OffHand", state.stashedOffHand.save(self.registryAccess()));
        compound.put("BlockySiegeStashedHands", stash);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void blockysiege$loadStashedHands(CompoundTag compound, CallbackInfo ci) {
        if (!compound.contains("BlockySiegeStashedHands", Tag.TAG_COMPOUND)) return;
        Mob self = (Mob) (Object) this;
        CompoundTag stash = compound.getCompound("BlockySiegeStashedHands");
        SiegeCrewState state = this.blockysiege$getCrewState();
        state.stashedMainHand = ItemStack.parseOptional(self.registryAccess(), stash.getCompound("MainHand"));
        state.stashedOffHand = ItemStack.parseOptional(self.registryAccess(), stash.getCompound("OffHand"));
    }

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void blockysiege$runSiegeCrew(CallbackInfo ci) {
        if (SiegeCrewAI.tick((Mob) (Object) this)) {
            ci.cancel();
        }
    }
}
