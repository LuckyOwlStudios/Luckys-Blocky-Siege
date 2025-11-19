package net.luckystudios.entity.custom.new_boats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.luckystudios.BlockySiege;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public class SloopRenderer extends EntityRenderer<SloopEntity> {

    private static final ResourceLocation SLOOP_TEXTURE = BlockySiege.id("textures/entity/boats/spruce_sloop.png");

    private final EntityModel<SloopEntity> sloopModel;

    public SloopRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.sloopModel = new SloopModel<>(context.bakeLayer(SloopModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(SloopEntity sloopEntity) {
        return SLOOP_TEXTURE;
    }

    @Override
    public void render(SloopEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 2F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        float f = (float)entity.getHurtTime() - partialTicks;
        float f1 = entity.getDamage() - partialTicks;
        if (f1 < 0.0F) {
            f1 = 0.0F;
        }

        if (f > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f) * f * f1 / 10.0F * (float)entity.getHurtDir()));
        }

        float f2 = entity.getBubbleAngle(partialTicks);
        if (!Mth.equal(f2, 0.0F)) {
            poseStack.mulPose((new Quaternionf()).setAngleAxis(entity.getBubbleAngle(partialTicks) * ((float)Math.PI / 180F), 1.0F, 0.0F, 1.0F));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        sloopModel.setupAnim(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        sloopModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        if (!entity.isUnderWater()) {
//            VertexConsumer vertexconsumer1 = buffer.getBuffer(RenderType.waterMask());
//            if (sloopModel instanceof WaterPatchModel) {
//                WaterPatchModel waterpatchmodel = (WaterPatchModel)sloopModel;
//                waterpatchmodel.waterPatch().render(poseStack, vertexconsumer1, packedLight, OverlayTexture.NO_OVERLAY);
//            }
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
