package net.luckystudios.blocks.custom.shooting.multi_cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luckystudios.BlockySiege;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class MultiCannonModel<T extends Entity> extends EntityModel<T> {

	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/block/multi_cannon.png");

	private final ModelPart root;
	private final ModelPart yaw;
	private final ModelPart pitch;
	private final ModelPart barrel;
	private final ModelPart barrel_0;
	private final ModelPart barrel_1;
	private final ModelPart barrel_2;
	private final ModelPart barrel_3;

	public MultiCannonModel(ModelPart root) {
		this.root = root;
		this.yaw = root.getChild("yaw");
		this.pitch = this.yaw.getChild("pitch");
		this.barrel = this.pitch.getChild("barrel");
		this.barrel_0 = this.barrel.getChild("barrel_0");
		this.barrel_1 = this.barrel.getChild("barrel_1");
		this.barrel_2 = this.barrel.getChild("barrel_2");
		this.barrel_3 = this.barrel.getChild("barrel_3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition yaw = partdefinition.addOrReplaceChild("yaw", CubeListBuilder.create().texOffs(0, 38).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(0, 72).mirror().addBox(-8.0F, -16.0F, -8.0F, 2.0F, 14.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 72).addBox(6.0F, -16.0F, -8.0F, 2.0F, 14.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition pitch = yaw.addOrReplaceChild("pitch", CubeListBuilder.create().texOffs(0, 56).addBox(-9.0F, -4.0F, -4.0F, 18.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.5672F, 0.0F, 0.0F));

		PartDefinition barrel = pitch.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -6.0F, -10.0F, 12.0F, 12.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition barrel_0 = barrel.addOrReplaceChild("barrel_0", CubeListBuilder.create().texOffs(44, 4).addBox(-5.0F, -5.0F, -14.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(44, 13).addBox(-6.0F, -6.0F, -15.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition barrel_1 = barrel.addOrReplaceChild("barrel_1", CubeListBuilder.create().texOffs(44, 4).addBox(-5.0F, -5.0F, -14.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(44, 13).addBox(-5.0F, -6.0F, -15.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 0.0F, 0.0F));

		PartDefinition barrel_2 = barrel.addOrReplaceChild("barrel_2", CubeListBuilder.create().texOffs(44, 4).addBox(-5.0F, -5.0F, -14.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(44, 13).addBox(-6.0F, -5.0F, -15.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition barrel_3 = barrel.addOrReplaceChild("barrel_3", CubeListBuilder.create().texOffs(44, 4).addBox(-5.0F, -5.0F, -14.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(44, 13).addBox(-5.0F, -5.0F, -15.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 5.0F, 0.0F));

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