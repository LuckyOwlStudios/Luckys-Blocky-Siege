package net.luckystudios.entity.custom.block_projectile.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.block_projectile.BlockProjectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * A special behavior for a block fired from a cannon. Behaviors are declared in data packs, see
 * {@link BlockProjectileBehaviorManager}. Register new behavior types with {@link #registerType}.
 */
public interface BlockProjectileBehavior {

    Map<ResourceLocation, MapCodec<? extends BlockProjectileBehavior>> TYPES = new HashMap<>();

    /** Dispatches on the "type" field, so the behavior's own fields sit next to it in the json. */
    MapCodec<BlockProjectileBehavior> MAP_CODEC = ResourceLocation.CODEC.dispatchMap("type",
            BlockProjectileBehavior::typeId,
            id -> {
                MapCodec<? extends BlockProjectileBehavior> codec = TYPES.get(id);
                if (codec == null) {
                    throw new IllegalStateException("Unknown block projectile behavior type: " + id);
                }
                return codec;
            });

    Codec<BlockProjectileBehavior> CODEC = MAP_CODEC.codec();

    static void registerType(ResourceLocation id, MapCodec<? extends BlockProjectileBehavior> codec) {
        TYPES.put(id, codec);
    }

    static void bootstrap() {
        registerType(BlockySiege.id("break_into_drops"), BreakIntoDrops.CODEC);
        registerType(BlockySiege.id("explode"), Explode.CODEC);
    }

    ResourceLocation typeId();

    /** Called on the server when the projectile hits something. The projectile is discarded afterwards. */
    void onImpact(ServerLevel level, Vec3 pos, BlockProjectile projectile);
}
