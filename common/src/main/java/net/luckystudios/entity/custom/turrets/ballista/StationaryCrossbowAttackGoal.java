package net.luckystudios.entity.custom.turrets.ballista;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Charges the installed crossbow exactly like a player would (start using it, hold for the
 * crossbow's own charge duration, release), so quick charge and any modded charge-time changes
 * apply, then fires once the turret is lined up with the predicted, arcing aim.
 */
public class StationaryCrossbowAttackGoal<T extends BallistaEntity> extends Goal {
    /** How closely the turret has to be lined up (degrees) before it lets go. */
    private static final float AIM_TOLERANCE = 4.0F;
    private static final float TURN_SPEED = 30.0F;

    private final T ballista;
    private int attackDelay;

    public StationaryCrossbowAttackGoal(T ballista) {
        this.ballista = ballista;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.isValidTarget() || !this.ballista.hasCrossbow()) {
            return false;
        }
        ItemStack weapon = this.ballista.getMainHandItem();
        return CrossbowItem.isCharged(weapon) || this.ballista.hasProjectile(weapon);
    }

    private boolean isValidTarget() {
        LivingEntity target = this.ballista.getTarget();
        return target != null && target.isAlive()
                && this.ballista.distanceToSqr(target) <= BallistaEntity.ATTACK_RANGE * BallistaEntity.ATTACK_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.ballista.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.ballista.setAggressive(false);
        if (this.ballista.isUsingItem()) {
            // Not loaded yet, so nothing is lost; a loaded crossbow stays loaded for the next target.
            this.ballista.stopUsingItem();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.ballista.getTarget();
        if (target == null) {
            return;
        }
        boolean canSee = this.ballista.getSensing().hasLineOfSight(target);

        BallistaAiming.Solution aim = this.ballista.solveAim(target);
        Vec3 lookAt = this.ballista.getEyePosition().add(aim.direction().scale(16.0));
        this.ballista.getLookControl().setLookAt(lookAt.x, lookAt.y, lookAt.z, TURN_SPEED, TURN_SPEED);

        ItemStack weapon = this.ballista.getMainHandItem();
        if (CrossbowItem.isCharged(weapon)) {
            if (this.attackDelay > 0) {
                this.attackDelay--;
            } else if (canSee && this.isLinedUp(aim)) {
                this.ballista.performRangedAttack(target, 1.0F);
            }
        } else if (!this.ballista.isUsingItem()) {
            if (canSee && this.ballista.hasProjectile(weapon)) {
                this.ballista.startUsingItem(InteractionHand.MAIN_HAND);
                this.ballista.setChargingCrossbow(true);
            }
        } else if (this.ballista.getTicksUsingItem() >= CrossbowItem.getChargeDuration(weapon, this.ballista)) {
            // Releasing runs the crossbow's own loading code, which pulls ammo via BallistaEntity#getProjectile.
            this.ballista.releaseUsingItem();
            this.ballista.setChargingCrossbow(false);
            this.ballista.getBallistaBlockEntity().ifPresent(be -> be.inventory.setChanged());
            this.attackDelay = 5 + this.ballista.getRandom().nextInt(10);
        }
    }

    private boolean isLinedUp(BallistaAiming.Solution aim) {
        float yawError = Math.abs(Mth.wrapDegrees(this.ballista.getYHeadRot() - aim.yRot()));
        float pitchError = Math.abs(this.ballista.getXRot() - aim.xRot());
        return yawError <= AIM_TOLERANCE && pitchError <= AIM_TOLERANCE;
    }
}
