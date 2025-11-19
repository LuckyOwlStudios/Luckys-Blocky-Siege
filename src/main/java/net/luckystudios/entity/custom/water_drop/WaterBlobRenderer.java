package net.luckystudios.entity.custom.water_drop;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luckystudios.BlockySiege;
import net.luckystudios.entity.ImprovedProjectile;
import net.luckystudios.util.ModModelLayers;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class WaterBlobRenderer<T extends ImprovedProjectile> extends EntityRenderer<T> {

    public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/projectile/water_blob.png");

    private final WaterBlobModel model;

    public WaterBlobRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WaterBlobModel(context.bakeLayer(ModModelLayers.WATER_BLOB));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE_LOCATION;
    }

    public int getColor(T entity) {
        return BiomeColors.getAverageWaterColor(entity.level(), entity.blockPosition());
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.pushPose();

        // Create blob animation using sine waves for stretching/squishing
        float time = entity.tickCount + partialTick;

        // Primary blob oscillation - slower, larger movement
        float primaryScale = 1.0F + (float)Math.sin(time) * 0.15F;

        // Secondary oscillation - faster, smaller movement for more organic feel
        float secondaryScale = 1.0F + (float)Math.sin(time) * 0.08F;

        // Apply different scaling to different axes for more blob-like effect
        float scaleX = primaryScale * secondaryScale;
        float scaleY = (2.0F - primaryScale) * (2.0F - secondaryScale); // Inverse scaling for Y
        float scaleZ = primaryScale * secondaryScale;

        // Add slight randomness based on entity ID for variation
        float randomOffset = entity.getId() * 0.1F;
        scaleX += (float)Math.sin(time * 0.15F + randomOffset) * 0.05F;
        scaleZ += (float)Math.cos(time * 0.15F + randomOffset) * 0.05F;

        poseStack.scale(scaleX, scaleY, scaleZ);

        poseStack.translate(0,-1.25,0);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(entity))).setColor(1,0,0,1);
        model.setupAnim(entity, 0.0F, 0.0F, time, 0.0F, 0.0F);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, getColor(entity));

        poseStack.popPose();
    }
}
