package net.luckystudios.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A loader-independent amount of fluid (in millibuckets) with optional data components, such as potion contents.
 * Saves in the same format as NeoForge's FluidStack so existing worlds load unchanged.
 */
public final class FluidStack {
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0, DataComponentPatch.EMPTY);

    public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidStack::getFluid),
            Codec.INT.fieldOf("amount").forGetter(FluidStack::getAmount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStack::getComponentsPatch)
    ).apply(instance, FluidStack::new));

    private final Fluid fluid;
    private int amount;
    private final PatchedDataComponentMap components;

    public FluidStack(Fluid fluid, int amount) {
        this(fluid, amount, DataComponentPatch.EMPTY);
    }

    private FluidStack(Fluid fluid, int amount, DataComponentPatch patch) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch);
    }

    public Fluid getFluid() {
        return isEmpty() ? Fluids.EMPTY : fluid;
    }

    public int getAmount() {
        return isEmpty() ? 0 : amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void grow(int amount) {
        this.amount += amount;
    }

    public void shrink(int amount) {
        this.amount -= amount;
    }

    public boolean isEmpty() {
        return this == EMPTY || fluid == Fluids.EMPTY || amount <= 0;
    }

    @Nullable
    public <T> T get(DataComponentType<? extends T> type) {
        return components.get(type);
    }

    public <T> T getOrDefault(DataComponentType<? extends T> type, T defaultValue) {
        return components.getOrDefault(type, defaultValue);
    }

    public boolean has(DataComponentType<?> type) {
        return components.has(type);
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> type, @Nullable T value) {
        return components.set(type, value);
    }

    public DataComponentPatch getComponentsPatch() {
        return components.asPatch();
    }

    public FluidStack copy() {
        return isEmpty() ? EMPTY : new FluidStack(fluid, amount, components.asPatch());
    }

    public FluidStack copyWithAmount(int amount) {
        return isEmpty() ? EMPTY : new FluidStack(fluid, amount, components.asPatch());
    }

    public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
        return first.getFluid() == second.getFluid() && Objects.equals(first.components, second.components);
    }

    public Tag save(HolderLookup.Provider registries) {
        return CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    public static FluidStack parseOptional(HolderLookup.Provider registries, CompoundTag tag) {
        if (tag.isEmpty()) {
            return EMPTY;
        }
        return CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(EMPTY);
    }
}
