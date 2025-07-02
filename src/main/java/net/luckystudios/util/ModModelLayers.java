package net.luckystudios.util;

import com.google.common.collect.Sets;
import net.luckystudios.BlockySiege;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class ModModelLayers {
    private static final Set<ModelLayerLocation> ALL_MODELS = Sets.newHashSet();
    public static final ModelLayerLocation CANNON = register("cannon");
    public static final ModelLayerLocation MULTI_CANNON = register("multi_cannon");
    public static final ModelLayerLocation CANNON_BALL = register("cannon_ball");
    public static final ModelLayerLocation IRON_GATE = register("iron_gate");

    private static ModelLayerLocation register(String path) {
        return register(path, "main");
    }

    private static ModelLayerLocation register(String path, String model) {
        ModelLayerLocation modellayerlocation = createLocation(path, model);
        if (!ALL_MODELS.add(modellayerlocation)) {
            throw new IllegalStateException("Duplicate registration for " + modellayerlocation);
        } else {
            return modellayerlocation;
        }
    }

    private static ModelLayerLocation createLocation(String path, String model) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, path), model);
    }
}
