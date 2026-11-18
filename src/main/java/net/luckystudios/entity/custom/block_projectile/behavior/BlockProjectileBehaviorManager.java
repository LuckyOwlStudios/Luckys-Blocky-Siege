package net.luckystudios.entity.custom.block_projectile.behavior;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.luckystudios.BlockySiege;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads block projectile behaviors from {@code data/<namespace>/blockysiege/block_projectile_behaviors/*.json}.
 * <pre>
 * {
 *   "required_mod": "farmersdelight",        // optional, file is skipped silently if that mod isn't loaded
 *   "blocks": ["minecraft:melon", "#c:some_tag"],
 *   "type": "blockysiege:break_into_drops",  // see BlockProjectileBehavior types, remaining fields depend on the type
 *   "drops": [{"item": "minecraft:melon_slice", "min": 3, "max": 7}]
 * }
 * </pre>
 */
@EventBusSubscriber(modid = BlockySiege.MOD_ID)
public class BlockProjectileBehaviorManager extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "blockysiege/block_projectile_behaviors";
    private static final Gson GSON = new GsonBuilder().create();
    public static final BlockProjectileBehaviorManager INSTANCE = new BlockProjectileBehaviorManager();

    /** One json file: which blocks it applies to, and what happens to them. */
    public record Entry(Optional<String> requiredMod, List<String> blocks, BlockProjectileBehavior behavior) {
        public static final MapCodec<Entry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("required_mod").forGetter(Entry::requiredMod),
                Codec.STRING.listOf().fieldOf("blocks").forGetter(Entry::blocks),
                BlockProjectileBehavior.MAP_CODEC.forGetter(Entry::behavior)
        ).apply(instance, Entry::new));

        public static final Codec<Entry> FILE_CODEC = CODEC.codec();

        boolean matches(BlockState state) {
            for (String id : blocks) {
                if (id.startsWith("#")) {
                    ResourceLocation tagId = ResourceLocation.tryParse(id.substring(1));
                    if (tagId != null && state.is(TagKey.create(Registries.BLOCK, tagId))) return true;
                } else {
                    ResourceLocation blockId = ResourceLocation.tryParse(id);
                    if (blockId != null && BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(blockId)) return true;
                }
            }
            return false;
        }
    }

    private List<Entry> entries = List.of();

    private BlockProjectileBehaviorManager() {
        super(GSON, DIRECTORY);
    }

    public static Optional<BlockProjectileBehavior> getBehavior(BlockState state) {
        for (Entry entry : INSTANCE.entries) {
            if (entry.matches(state)) return Optional.of(entry.behavior());
        }
        return Optional.empty();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<Entry> loaded = new ArrayList<>();
        objects.forEach((id, json) -> {
            // Compat files are skipped without a fuss when their mod isn't installed
            if (json.isJsonObject() && json.getAsJsonObject().has("required_mod")) {
                String mod = json.getAsJsonObject().get("required_mod").getAsString();
                if (!ModList.get().isLoaded(mod)) return;
            }
            try {
                Entry.FILE_CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> BlockySiege.LOGGER.error("Failed to load block projectile behavior {}: {}", id, error))
                        .ifPresent(loaded::add);
            } catch (RuntimeException e) {
                BlockySiege.LOGGER.error("Failed to load block projectile behavior {}", id, e);
            }
        });
        this.entries = List.copyOf(loaded);
        BlockySiege.LOGGER.info("Loaded {} block projectile behaviors", loaded.size());
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }
}
