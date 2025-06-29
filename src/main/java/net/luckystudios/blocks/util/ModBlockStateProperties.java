package net.luckystudios.blocks.util;

import net.luckystudios.blocks.util.enums.CannonPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ModBlockStateProperties {
    public static final EnumProperty<CannonPart> CANNON_PART = EnumProperty.create("part", CannonPart.class);
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

}
