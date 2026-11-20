package net.luckystudios.entity.ai.crew;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** What one crew mob is doing with a siege weapon. Lives on the mob (see {@link SiegeCrewHolder}) and is never saved. */
public class SiegeCrewState {

    public enum Phase {
        /** Not involved with a weapon; the mob's own AI runs. */
        NONE,
        /** Walking to a claimed weapon. */
        APPROACH,
        /** Riding the weapon's seat and operating it. */
        MANNING
    }

    public Phase phase = Phase.NONE;
    @Nullable
    public BlockPos weapon;
    @Nullable
    public LivingEntity target;
    /** True when the mob manned the weapon without a target, to keep watch. */
    public boolean guarding;

    public int phaseTicks;
    public int searchCooldown;
    public int noTargetTicks;
    public int retargetCooldown;
    public int fireCooldown;
    public int reloadTicks;
    public int noAmmoTicks;
    public int syncCooldown;
    public float syncedYaw = Float.NaN;
    public float syncedPitch = Float.NaN;
    public int repathCooldown;
    public double closestApproach = Double.MAX_VALUE;
    public int ticksSinceProgress;

    /** Smoothed target velocity, for leading moving targets. */
    public Vec3 targetVelocity = Vec3.ZERO;
    @Nullable
    public Vec3 lastTargetPos;

    /** Idle sweep direction while guarding: +1 or -1. */
    public float sweepDirection = 1.0F;
    public float sweepCentre = Float.NaN;

    /**
     * What the mob was holding before it sat down. Crews keep their hands free while they work a weapon; this is
     * saved with the mob and handed back when it gets off, or put back before its drops are rolled if it dies.
     */
    public ItemStack stashedMainHand = ItemStack.EMPTY;
    public ItemStack stashedOffHand = ItemStack.EMPTY;

    /** Weapons this mob recently failed to use, with the game time until which they are ignored. */
    public final Map<BlockPos, Long> ignoredWeapons = new HashMap<>();

    public void reset() {
        this.phase = Phase.NONE;
        this.weapon = null;
        this.guarding = false;
        this.phaseTicks = 0;
        this.noTargetTicks = 0;
        this.retargetCooldown = 0;
        this.fireCooldown = 0;
        this.reloadTicks = 0;
        this.noAmmoTicks = 0;
        this.syncCooldown = 0;
        this.syncedYaw = Float.NaN;
        this.syncedPitch = Float.NaN;
        this.repathCooldown = 0;
        this.closestApproach = Double.MAX_VALUE;
        this.ticksSinceProgress = 0;
        this.targetVelocity = Vec3.ZERO;
        this.lastTargetPos = null;
        this.sweepCentre = Float.NaN;
    }
}
