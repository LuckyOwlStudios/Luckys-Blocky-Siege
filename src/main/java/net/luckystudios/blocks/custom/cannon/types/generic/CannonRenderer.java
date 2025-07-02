package net.luckystudios.blocks.custom.cannon.types.generic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.luckystudios.BlockySiege;
import net.luckystudios.util.ModModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class CannonRenderer implements BlockEntityRenderer<CannonBlockEntity> {

	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/block/cannon.png");

	private final ModelPart yaw;
	private final ModelPart pitch;
	private final ModelPart barrel;
	private final ModelPart tip;

	public CannonRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart modelpart = context.bakeLayer(ModModelLayers.CANNON);
		this.yaw = modelpart.getChild("yaw");
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
	// Copied from the BlockEntityRenderer class
	// Copied from the EnchantTableRenderer
	@Override
	public void render(CannonBlockEntity cannonBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(TEXTURE_LOCATION));

		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F)); // Flip model upright

		// Apply yaw and pitch
		float yawDeg = interpolateAngleDeg(cannonBlockEntity.displayOYaw, cannonBlockEntity.displayYaw, partialTick);
		float pitchDeg = Mth.lerp(partialTick, cannonBlockEntity.displayOPitch, cannonBlockEntity.displayPitch);

		// Apply degrees → radians conversion here, since ModelPart uses radians:
		this.yaw.yRot = (float) Math.toRadians(yawDeg);
		this.pitch.xRot = (float) -Math.toRadians(pitchDeg);

		// Render full model from the root
		this.yaw.render(poseStack, vertexConsumer, packedLight, packedOverlay);

		poseStack.popPose();
	}

	private float interpolateAngleDeg(float from, float to, float partialTick) {
		float delta = to - from;
		while (delta < -180.0F) delta += 360.0F;
		while (delta >= 180.0F) delta -= 360.0F;
		return from + delta * partialTick;
	}

	@Override
	public AABB getRenderBoundingBox(CannonBlockEntity blockEntity) {
		return new AABB(blockEntity.getBlockPos()).inflate(1);
	}

	@Override
	public int getViewDistance() {
		return 128;
	}
}