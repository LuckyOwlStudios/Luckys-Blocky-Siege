package net.luckystudios.entity.custom.turrets.ballista;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luckystudios.BlockySiege;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Crackiness;

import java.util.Map;

/**
 * Draws crack overlays on a damaged ballista, the same way the iron golem does:
 * low under 75% health, medium under 50%, high under 25%.
 */
public class BallistaCrackinessLayer extends RenderLayer<BallistaEntity, BallistaGolemModel<BallistaEntity>> {

    private static final Map<Crackiness.Level, ResourceLocation> CRACKS = Map.of(
            Crackiness.Level.LOW, BlockySiege.id("textures/entity/ballista/ballista_crackiness_low.png"),
            Crackiness.Level.MEDIUM, BlockySiege.id("textures/entity/ballista/ballista_crackiness_medium.png"),
            Crackiness.Level.HIGH, BlockySiege.id("textures/entity/ballista/ballista_crackiness_high.png")
    );

    public BallistaCrackinessLayer(RenderLayerParent<BallistaEntity, BallistaGolemModel<BallistaEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, BallistaEntity ballista,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (ballista.isInvisible()) return;
        Crackiness.Level level = Crackiness.GOLEM.byFraction(ballista.getHealth() / ballista.getMaxHealth());
        if (level == Crackiness.Level.NONE) return;
        renderColoredCutoutModel(getParentModel(), CRACKS.get(level), poseStack, buffer, packedLight, ballista, -1);
    }
}
