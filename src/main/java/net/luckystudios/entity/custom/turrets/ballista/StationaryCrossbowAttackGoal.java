package net.luckystudios.entity.custom.turrets.ballista;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;

import java.util.EnumSet;

public class StationaryCrossbowAttackGoal<T extends BallistaEntity> extends Goal {
    private StationaryCrossbowAttackGoal.CrossbowState crossbowState;
    private final T ballista;
    private int seeTime;
    private int attackDelay;
    private int attackRadiusSqr = 32;

    public StationaryCrossbowAttackGoal(T ballista) {
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

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.ballista.getTarget();
        if (livingentity != null) {
            boolean flag = this.ballista.getSensing().hasLineOfSight(livingentity);
            boolean flag1 = this.seeTime > 0;
            if (flag != flag1) {
                this.seeTime = 0;
            }

            if (flag) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            double d0 = this.ballista.distanceToSqr(livingentity);
            boolean flag2 = (d0 > (double)this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;

            this.ballista.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            if (this.crossbowState == StationaryCrossbowAttackGoal.CrossbowState.UNCHARGED) {
                if (!flag2) {
                    this.ballista.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.ballista, (item) -> item instanceof CrossbowItem));
                    this.crossbowState = StationaryCrossbowAttackGoal.CrossbowState.CHARGING;
                    this.ballista.setChargingCrossbow(true);
                }
            } else if (this.crossbowState == StationaryCrossbowAttackGoal.CrossbowState.CHARGING) {
                if (!this.ballista.isUsingItem()) {
                    this.crossbowState = StationaryCrossbowAttackGoal.CrossbowState.UNCHARGED;
                }

                int i = this.ballista.getTicksUsingItem();
                ItemStack itemstack = this.ballista.getUseItem();
                if (i >= CrossbowItem.getChargeDuration(itemstack, this.ballista)) {
                    this.ballista.releaseUsingItem();
                    this.crossbowState = StationaryCrossbowAttackGoal.CrossbowState.CHARGED;
                    this.attackDelay = 20 + this.ballista.getRandom().nextInt(20);
                    this.ballista.setChargingCrossbow(false);
                }
            } else if (this.crossbowState == StationaryCrossbowAttackGoal.CrossbowState.CHARGED) {
                --this.attackDelay;
                if (this.attackDelay == 0) {
                    this.crossbowState = StationaryCrossbowAttackGoal.CrossbowState.READY_TO_ATTACK;
                }
            } else if (this.crossbowState == StationaryCrossbowAttackGoal.CrossbowState.READY_TO_ATTACK && flag) {
                this.ballista.performRangedAttack(livingentity, 1.0F);
                this.crossbowState = StationaryCrossbowAttackGoal.CrossbowState.UNCHARGED;
            }
        }
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}