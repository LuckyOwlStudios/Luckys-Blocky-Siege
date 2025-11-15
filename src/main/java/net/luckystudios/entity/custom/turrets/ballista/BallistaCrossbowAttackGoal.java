package net.luckystudios.entity.custom.turrets.ballista;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;

import java.util.EnumSet;

public class BallistaCrossbowAttackGoal<T extends BallistaEntity> extends Goal {
    private final T ballista;
    private int seeTime;
    private int attackDelay;
    private int attackRadiusSqr = 32;



    public BallistaCrossbowAttackGoal(T ballista) {
        this.ballista = ballista;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    public boolean canUse() {
        return this.isValidTarget() && this.ballista.hasCrossbow();
    }

    private boolean isValidTarget() {
        return this.ballista.getTarget() != null && this.ballista.getTarget().isAlive();
    }

    public boolean canContinueToUse() {
        return this.isValidTarget() && this.canUse();
    }

    public void stop() {
        super.stop();
        this.ballista.setAggressive(false);
        this.ballista.setTarget(null);
        this.seeTime = 0;
        if (this.ballista.isUsingItem()) {
            this.ballista.stopUsingItem();
            this.ballista.setChargingCrossbow(false);
            this.ballista.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }

    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity livingentity = this.ballista.getTarget();
        if (livingentity != null) {
            boolean can_see_target = this.ballista.getSensing().hasLineOfSight(livingentity);
            boolean is_still_seeing = this.seeTime > 0;
            if (can_see_target != is_still_seeing) {
                this.seeTime = 0;
            }

            // Handle see time
            if (can_see_target) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            double distance_to_target = this.ballista.distanceToSqr(livingentity);
            boolean flag2 = (distance_to_target > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;

            this.ballista.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            if (this.ballista.getCrossbowState() == BallistaEntity.CrossbowState.UNCHARGED.ordinal()) {
                if (!flag2) {
                    this.ballista.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.ballista, (item) -> item instanceof CrossbowItem));
                    this.ballista.setCrossbowState(BallistaEntity.CrossbowState.CHARGING.ordinal());
                    ((CrossbowAttackMob) this.ballista).setChargingCrossbow(true);
                }
            } else if (this.ballista.getCrossbowState() == BallistaEntity.CrossbowState.CHARGING.ordinal()) {
                if (!this.ballista.isUsingItem()) {
                    this.ballista.setCrossbowState(BallistaEntity.CrossbowState.UNCHARGED.ordinal());
                }

                int timeUsingItem = this.ballista.getTicksUsingItem();
                ItemStack itemstack = this.ballista.getUseItem();
                if (timeUsingItem >= CrossbowItem.getChargeDuration(itemstack, this.ballista)) {
                    this.ballista.releaseUsingItem();
                    this.ballista.setCrossbowState(BallistaEntity.CrossbowState.CHARGED.ordinal());
                    this.attackDelay = 20 + this.ballista.getRandom().nextInt(20);
                    ((CrossbowAttackMob) this.ballista).setChargingCrossbow(false);
                }
            } else if (this.ballista.getCrossbowState() == BallistaEntity.CrossbowState.CHARGED.ordinal()) {
                --this.attackDelay;
                if (this.attackDelay == 0) {
                    this.ballista.setCrossbowState(BallistaEntity.CrossbowState.READY_TO_ATTACK.ordinal());
                }
            } else if (this.ballista.getCrossbowState() == BallistaEntity.CrossbowState.READY_TO_ATTACK.ordinal() && can_see_target) {
                ((RangedAttackMob) this.ballista).performRangedAttack(livingentity, 1.0F);
                this.ballista.setCrossbowState(BallistaEntity.CrossbowState.UNCHARGED.ordinal());
            }
        }

    }
}