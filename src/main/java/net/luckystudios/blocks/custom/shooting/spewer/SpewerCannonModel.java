package net.luckystudios.blocks.custom.shooting.spewer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class SpewerCannonModel<T extends Entity> extends EntityModel<T> {

    private final ModelPart root;
    private final ModelPart yaw;
    private final ModelPart cannon;
    private final ModelPart pitch;
    private final ModelPart barrel;

    public SpewerCannonModel(ModelPart root) {
        this.root = root;
        this.yaw = root.getChild("yaw");
        this.cannon = this.yaw.getChild("cannon");
        this.pitch = this.cannon.getChild("pitch");
        this.barrel = this.pitch.getChild("barrel");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition yaw = partdefinition.addOrReplaceChild("yaw", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cannon = yaw.addOrReplaceChild("cannon", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).mirror().addBox(-8.0F, -16.0F, -8.0F, 2.0F, 14.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(64, 0).addBox(6.0F, -16.0F, -8.0F, 2.0F, 14.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(-6.0F, -16.0F, -10.0F, 12.0F, 14.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(58, 96).addBox(-8.0F, -14.0F, 11.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(59, 97).addBox(3.0F, -14.0F, 10.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(59, 97).addBox(-5.0F, -14.0F, 10.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 78).addBox(-7.0F, -18.0F, -3.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition pitch = cannon.addOrReplaceChild("pitch", CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -9.0F, 4.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-9.0F, -4.0F, -4.0F, 18.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition barrel = pitch.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(18, 86).addBox(-3.0F, -3.0F, -15.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 85).addBox(-4.0F, -4.0F, -16.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

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
