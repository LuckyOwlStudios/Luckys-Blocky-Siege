package net.luckystudios.blocks.util;

import net.luckystudios.blocks.util.enums.DamageState;
import net.luckystudios.blocks.util.enums.FiringState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ModBlockStateProperties {
    public static final EnumProperty<DamageState> DAMAGE_STATE = EnumProperty.create("damage_state", DamageState.class);
    public static final EnumProperty<FiringState> FIRING_STATE = EnumProperty.create("firing_state", FiringState.class);
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

}
