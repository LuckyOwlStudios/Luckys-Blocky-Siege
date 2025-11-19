package net.luckystudios.entity.custom.new_boats;

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
	private final ModelPart paddle_left;
	private final ModelPart paddle_left2;
	private final ModelPart paddle_right;
	private final ModelPart paddle_right2;
	private final ModelPart mask;
	private final ModelPart water_patch;

	public SloopModel(ModelPart root) {
		this.ship = root.getChild("ship");
		this.bottom = this.ship.getChild("bottom");
		this.paddle_left = this.ship.getChild("paddle_left");
		this.paddle_left2 = this.ship.getChild("paddle_left2");
		this.paddle_right = this.ship.getChild("paddle_right");
		this.paddle_right2 = this.ship.getChild("paddle_right2");
		this.mask = this.ship.getChild("mask");
		this.water_patch = this.ship.getChild("water_patch");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ship = partdefinition.addOrReplaceChild("ship", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bottom = ship.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-30.0F, -17.0F, -1.0F, 64.0F, 32.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 80).addBox(14.0F, -9.0F, 3.0F, 16.0F, 16.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 46).addBox(-24.0F, -17.0F, -3.0F, 48.0F, 32.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(156, 0).addBox(9.0F, -18.0F, 13.0F, 26.0F, 34.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 126).addBox(10.0F, -17.0F, 15.0F, 24.0F, 32.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 162).addBox(-29.0F, -16.0F, 13.0F, 39.0F, 30.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -3.0F, 0.0F, 0.0F, 1.5708F, -1.5708F));

		PartDefinition bottom_r1 = bottom.addOrReplaceChild("bottom_r1", CubeListBuilder.create().texOffs(0, 231).addBox(-10.0F, -10.0F, -6.9F, 21.0F, 21.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(100, 46).addBox(-11.0F, -11.0F, -20.9F, 22.0F, 22.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-30.0F, -1.0F, 19.8F, 0.0F, 0.0F, -0.7854F));

		PartDefinition inner_r1 = bottom.addOrReplaceChild("inner_r1", CubeListBuilder.create().texOffs(0, 108).addBox(-6.0F, -6.0F, -1.0F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, -1.0F, 13.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition paddle_left = ship.addOrReplaceChild("paddle_left", CubeListBuilder.create().texOffs(216, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(240, 0).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, -11.0F, -7.0F, 2.1642F, 0.8727F, 2.8798F));

		PartDefinition paddle_left2 = ship.addOrReplaceChild("paddle_left2", CubeListBuilder.create().texOffs(216, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(240, 0).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, -11.0F, 13.0F, 2.1642F, 0.8727F, 2.8798F));

		PartDefinition paddle_right = ship.addOrReplaceChild("paddle_right", CubeListBuilder.create().texOffs(216, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(240, 0).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, -11.0F, -10.0F, 2.1642F, -0.8727F, -2.8798F));

		PartDefinition paddle_right2 = ship.addOrReplaceChild("paddle_right2", CubeListBuilder.create().texOffs(216, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(240, 0).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, -11.0F, 10.0F, 2.1642F, -0.8727F, -2.8798F));

		PartDefinition mask = ship.addOrReplaceChild("mask", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition inner_r2 = mask.addOrReplaceChild("inner_r2", CubeListBuilder.create().texOffs(104, 223).addBox(-24.0F, -30.0F, -1.0F, 48.0F, 32.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -32.0F, 3.0F, 0.0F, 0.0F, 0.0F));

		PartDefinition inner_r3 = mask.addOrReplaceChild("inner_r3", CubeListBuilder.create().texOffs(242, 22).addBox(0.0F, -24.0F, 0.0F, 2.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -30.0F, 0.0F, 0.0F, -1.5708F, 1.5708F));

		PartDefinition inner_r4 = mask.addOrReplaceChild("inner_r4", CubeListBuilder.create().texOffs(242, 22).addBox(0.0F, -24.0F, 0.0F, 2.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -60.0F, 0.0F, 0.0F, -1.5708F, 1.5708F));

		PartDefinition inner_r5 = mask.addOrReplaceChild("inner_r5", CubeListBuilder.create().texOffs(240, 20).addBox(-6.0F, -46.0F, -2.0F, 4.0F, 48.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.0F, 4.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition water_patch = ship.addOrReplaceChild("water_patch", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public ModelPart waterPatch() {
		return null;
	}

	@Override
	public void setupAnim(SloopEntity sloopEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
//		animatePaddle(sloopEntity, 0, this.paddle_left, limbSwing);
//		animatePaddle(sloopEntity, 0, this.paddle_left2, limbSwing);
//		animatePaddle(sloopEntity, 1, this.paddle_right, limbSwing);
//		animatePaddle(sloopEntity, 1, this.paddle_right2, limbSwing);
	}

	private static void animatePaddle(Boat boat, int side, ModelPart paddle, float limbSwing) {
		float f = boat.getRowingTime(side, limbSwing);
		paddle.xRot = Mth.clampedLerp((-(float)Math.PI / 3F), -0.2617994F, (Mth.sin(-f) + 1.0F) / 2.0F);
		paddle.yRot = Mth.clampedLerp((-(float)Math.PI / 4F), ((float)Math.PI / 4F), (Mth.sin(-f + 1.0F) + 1.0F) / 2.0F);
		if (side == 1) {
			paddle.yRot = (float)Math.PI - paddle.yRot;
		}

	}

	@Override
	public ModelPart root() {
		return this.ship;
	}
}