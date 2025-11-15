package net.luckystudios.blocks.custom.shooting.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class CannonModel<T extends Entity> extends EntityModel<T> {

    private final ModelPart root;
    private final ModelPart yaw;
    private final ModelPart pitch;
    private final ModelPart barrel;
    private final ModelPart tip;

    public CannonModel(ModelPart root) {
        this.root = root;
        this.yaw = root.getChild("yaw");
        this.pitch = this.yaw.getChild("pitch");
        this.barrel = this.pitch.getChild("barrel");
        this.tip = this.barrel.getChild("tip");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition yaw = partdefinition.addOrReplaceChild("yaw", CubeListBuilder.create().texOffs(0, 38).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 72).mirror().addBox(-8.0F, -16.0F, -8.0F, 2.0F, 14.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 72).addBox(6.0F, -16.0F, -8.0F, 2.0F, 14.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition pitch = yaw.addOrReplaceChild("pitch", CubeListBuilder.create().texOffs(0, 56).addBox(-9.0F, -4.0F, -4.0F, 18.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 0.0F));

        PartDefinition barrel = pitch.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -6.0F, -16.0F, 12.0F, 12.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(0, 21).addBox(-1.0F, -9.0F, 4.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tip = barrel.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(64, 38).addBox(-7.0F, -7.0F, 0.0F, 14.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -17.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T t, float v, float v1, float v2, float v3, float v4) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int i2) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    public void setRotations(float yaw, float pitch) {
        this.yaw.yRot = (float) Math.toRadians(yaw);
        this.pitch.xRot = (float) -Math.toRadians(pitch);
    }
}
