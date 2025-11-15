package net.luckystudios.blocks.util.enums;

import net.minecraft.util.StringRepresentable;

public enum FiringState implements StringRepresentable {
    OFF("off"),
    CHARGING("charging"),
    FIRED("fired")
    ;

    private final String name;

    FiringState(String name) {
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
