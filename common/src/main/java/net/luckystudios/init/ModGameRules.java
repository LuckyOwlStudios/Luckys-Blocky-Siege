package net.luckystudios.init;

import net.minecraft.world.level.GameRules;

public class ModGameRules {

    /** Whether mobs in the blockysiege:siege_crew tag (Pillagers, Vindicators and Piglins) man nearby siege weapons. */
    public static final GameRules.Key<GameRules.BooleanValue> RULE_SIEGE_WEAPON_CREWS =
            GameRules.register("doSiegeWeaponCrews", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));

    public static void init() {
    }
}
