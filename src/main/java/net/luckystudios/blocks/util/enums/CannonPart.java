package net.luckystudios.blocks.util.enums;

import net.minecraft.util.StringRepresentable;

public enum CannonPart implements StringRepresentable {
    MAIN("main"),
    BARREL("barrel");

    private final String name;

    private CannonPart(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
