package net.luckystudios.entity.custom.new_boats;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luckystudios.BlockySiege;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SloopModel<T extends SloopEntity> extends HierarchicalModel<T> implements WaterPatchModel {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(BlockySiege.id("sloop"), "main");

	private final ModelPart ship;
	private final ModelPart bottom;
	private final ModelPart post;
	private final ModelPart post_yaw;
	private final ModelPart sail;
	private final ModelPart left;
	private final ModelPart right;
	private final ModelPart front;
	private final ModelPart back;
	private final ModelPart paddle_left;
	private final ModelPart paddle_left2;
	private final ModelPart paddle_right;
	private final ModelPart paddle_right2;
	private final ModelPart water;

	public SloopModel(ModelPart root) {
		this.ship = root.getChild("ship");
		this.bottom = this.ship.getChild("bottom");
		this.post = this.ship.getChild("post");
		this.post_yaw = this.post.getChild("post_yaw");
		this.sail = this.post_yaw.getChild("sail");
		this.left = this.ship.getChild("left");
		this.right = this.ship.getChild("right");
		this.front = this.ship.getChild("front");
		this.back = this.ship.getChild("back");
		this.paddle_left = this.ship.getChild("paddle_left");
		this.paddle_left2 = this.ship.getChild("paddle_left2");
		this.paddle_right = this.ship.getChild("paddle_right");
		this.paddle_right2 = this.ship.getChild("paddle_right2");
		this.water = root.getChild("water");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ship = partdefinition.addOrReplaceChild("ship", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 0.0F));

		PartDefinition bottom = ship.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -29.0F, -1.0F, 32.0F, 56.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(70, 0).addBox(-4.0F, -4.0F, 2.0F, 8.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition post = ship.addOrReplaceChild("post", CubeListBuilder.create().texOffs(104, 0).addBox(-3.0F, -59.0F, -3.0F, 6.0F, 54.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition post_yaw = post.addOrReplaceChild("post_yaw", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = post_yaw.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(96, 4).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -56.0F, -4.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r2 = post_yaw.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(96, 4).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -22.0F, -4.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition sail = post_yaw.addOrReplaceChild("sail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = sail.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 95).addBox(-24.0F, -16.0F, 0.0F, 48.0F, 32.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -39.0F, -5.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition left = ship.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 59).addBox(-29.0F, -10.0F, -18.0F, 56.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(70, 53).mirror().addBox(9.0F, -13.0F, -18.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(70, 53).mirror().addBox(-15.0F, -13.0F, -18.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition right = ship.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 59).mirror().addBox(-27.0F, -10.0F, -18.0F, 56.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(70, 53).mirror().addBox(9.0F, -13.0F, -18.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(70, 53).mirror().addBox(-15.0F, -13.0F, -18.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition front = ship.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 84).addBox(-16.0F, -11.0F, -32.0F, 32.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(98, 95).addBox(-4.0F, -11.0F, -35.0F, 8.0F, 11.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(70, 76).addBox(-4.0F, -19.0F, -40.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition back = ship.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 70).addBox(-18.0F, -14.0F, 27.0F, 36.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition paddle_left = ship.addOrReplaceChild("paddle_left", CubeListBuilder.create().texOffs(88, 75).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(112, 80).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, -12.0F, 12.0F, 2.1642F, 0.8727F, 2.8798F));

		PartDefinition paddle_left2 = ship.addOrReplaceChild("paddle_left2", CubeListBuilder.create().texOffs(88, 75).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(112, 80).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, -12.0F, -12.0F, 2.1642F, 0.8727F, 2.8798F));

		PartDefinition paddle_right = ship.addOrReplaceChild("paddle_right", CubeListBuilder.create().texOffs(88, 75).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(112, 80).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.0F, -12.0F, -12.0F, 2.1642F, -0.8727F, -2.8798F));

		PartDefinition paddle_right2 = ship.addOrReplaceChild("paddle_right2", CubeListBuilder.create().texOffs(88, 75).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(112, 80).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.0F, -12.0F, 12.0F, 2.1642F, -0.8727F, -2.8798F));

		PartDefinition water = partdefinition.addOrReplaceChild("water", CubeListBuilder.create().texOffs(0, 64).addBox(-15.0F, -11.0F, -29.0F, 30.0F, 8.0F, 56.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
		this.ship.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public ModelPart waterPatch() {
		return this.water;
	}

	@Override
	public void setupAnim(SloopEntity sloopEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		animatePaddle(sloopEntity, 0, this.paddle_left, limbSwing);
		animatePaddle(sloopEntity, 0, this.paddle_left2, limbSwing);
		animatePaddle(sloopEntity, 1, this.paddle_right, limbSwing);
		animatePaddle(sloopEntity, 1, this.paddle_right2, limbSwing);
	}

	private static void animatePaddle(Boat boat, int side, ModelPart paddle, float limbSwing) {
		float f = boat.getRowingTime(side, limbSwing);
		paddle.xRot = -Mth.clampedLerp((-(float)Math.PI / 3F), -0.2617994F, (Mth.sin(-f) + 1.0F) / 2.0F);
		paddle.yRot = Mth.clampedLerp((-(float)Math.PI / 4F), ((float)Math.PI / 4F), (Mth.sin(-f + 1.0F) + 1.0F) / 2.0F) - 1;
		if (side == 1) {
			paddle.yRot = (float)Math.PI - paddle.yRot;
		}
	}

	@Override
	public ModelPart root() {
		return this.ship;
	}
}