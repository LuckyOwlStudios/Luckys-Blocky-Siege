package net.luckystudios.blocks.util.enums;

import net.minecraft.util.StringRepresentable;

public enum DamageState implements StringRepresentable {
    NONE("none"),
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high")
    ;

    private final String name;

    private DamageState(String name) {
        this.name = name;
    }

    // Convert integer back to DamageState using ordinal
    public static DamageState fromOrdinal(int ordinal) {
        DamageState[] values = DamageState.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        // Clamp to valid range
        return ordinal < 0 ? NONE : HIGH;
    }

    // Optional: convenience method for repair (decrease ordinal by 1)
    public DamageState repair() {
        return fromOrdinal(Math.max(0, this.ordinal() - 1));
    }

    // Optional: convenience method for damage (increase ordinal by 1)
    public DamageState damage() {
        return fromOrdinal(Math.min(DamageState.values().length - 1, this.ordinal() + 1));
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
