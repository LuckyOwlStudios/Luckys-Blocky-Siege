package net.luckystudios.datagen.types;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.block_projectile.behavior.BlockProjectileBehaviorManager;
import net.luckystudios.entity.custom.block_projectile.behavior.BreakIntoDrops;
import net.luckystudios.entity.custom.block_projectile.behavior.BreakIntoDrops.Drop;
import net.luckystudios.entity.custom.block_projectile.behavior.Explode;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the block projectile behavior json files. To add a new behavior for your own block,
 * call {@link #add} from {@link #addBehaviors()}, or drop a json with the same layout in your own data pack.
 */
public class ModBlockProjectileBehaviorProvider implements DataProvider {

    private static final String FARMERS_DELIGHT = "farmersdelight";

    private final PackOutput output;
    private final List<CompletableFuture<?>> pending = new ArrayList<>();
    private CachedOutput cachedOutput;

    public ModBlockProjectileBehaviorProvider(PackOutput output) {
        this.output = output;
    }

    protected void addBehaviors() {
        // Vanilla
        add("tnt", null, "minecraft:tnt", new Explode(4.0F, false));
        add("dried_kelp_block", null, "minecraft:dried_kelp_block", breaks("minecraft:dried_kelp", 9, 9));
        add("melon", null, "minecraft:melon", breaks("minecraft:melon_slice", 3, 7));

        // Farmer's Delight, these files are skipped when the mod isn't installed
        crate("carrot_crate", "minecraft:carrot");
        crate("potato_crate", "minecraft:potato");
        crate("beetroot_crate", "minecraft:beetroot");
        crate("cabbage_crate", "farmersdelight:cabbage");
        crate("tomato_crate", "farmersdelight:tomato");
        crate("onion_crate", "farmersdelight:onion");
        crate("rice_bag", "farmersdelight:rice");
    }

    private void crate(String block, String item) {
        add("farmersdelight/" + block, FARMERS_DELIGHT, FARMERS_DELIGHT + ":" + block, breaks(item, 9, 9));
    }

    private static BreakIntoDrops breaks(String itemId, int min, int max) {
        return new BreakIntoDrops(List.of(new Drop(ResourceLocation.parse(itemId), min, max)));
    }

    protected void add(String name, String requiredMod, String block, net.luckystudios.entity.custom.block_projectile.behavior.BlockProjectileBehavior behavior) {
        BlockProjectileBehaviorManager.Entry entry = new BlockProjectileBehaviorManager.Entry(Optional.ofNullable(requiredMod), List.of(block), behavior);
        JsonElement json = BlockProjectileBehaviorManager.Entry.FILE_CODEC.encodeStart(JsonOps.INSTANCE, entry)
                .getOrThrow(error -> new IllegalStateException("Could not encode block projectile behavior " + name + ": " + error));
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(BlockySiege.MOD_ID)
                .resolve(BlockProjectileBehaviorManager.DIRECTORY)
                .resolve(name + ".json");
        pending.add(DataProvider.saveStable(cachedOutput, json, path));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.cachedOutput = cache;
        this.pending.clear();
        this.addBehaviors();
        return CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Block Projectile Behaviors";
    }
}
