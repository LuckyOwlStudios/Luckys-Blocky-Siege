package net.luckystudios.blocks.custom.cannon.types.generic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.luckystudios.BlockySiege;
import net.luckystudios.util.ModModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import net.minecraft.client.model.HierarchicalModel;

public class CannonRenderer implements BlockEntityRenderer<CannonBlockEntity> {

	private final CustomHierarchicalModel model;
	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/block/cannon.png");

	public CannonRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new CustomHierarchicalModel(context.bakeLayer(ModModelLayers.CANNON));
	}

	private void updateRenderState(CannonBlockEntity cannonBlockEntity) {
		Level level = cannonBlockEntity.getLevel();
		if (level == null) return;
		int tickCount = (int) level.getGameTime();
		cannonBlockEntity.fireAnimation.start(tickCount);
	}

	// Copied from the BlockEntityRenderer class
	// Copied from the EnchantTableRenderer
	@Override
	public void render(CannonBlockEntity cannonBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
		Level level = cannonBlockEntity.getLevel();
		if (level == null) return;
		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F)); // Flip model upright

		// Apply yaw and pitch
		float yawDeg = interpolateAngleDeg(cannonBlockEntity.displayOYaw, cannonBlockEntity.displayYaw, partialTick);
		float pitchDeg = Mth.lerp(partialTick, cannonBlockEntity.displayOPitch, cannonBlockEntity.displayPitch);
		// Grabbing the vertex consumer/texture
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(TEXTURE_LOCATION));
		// Setting up animations!
		if (cannonBlockEntity.animationTime > 0) {
			updateRenderState(cannonBlockEntity);
		}
        model.setupBlockEntityAnim(cannonBlockEntity, level.getGameTime() + partialTick);

		// Render full model from the root
		model.setRotations(yawDeg, pitchDeg);
		model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay);

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

	private static final class CustomHierarchicalModel extends CannonModel {
		private final ModelPart root;
		private final BlockEntityHierarchicalModel animator = new BlockEntityHierarchicalModel();

		public CustomHierarchicalModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		public void setupBlockEntityAnim(CannonBlockEntity blockEntity, float ageInTicks) {
			animator.setupBlockEntityAnim(blockEntity, ageInTicks);
			super.setupAnim(null, 0, 0, ageInTicks, 0, 0);
		}

		public ModelPart getRoot() {
			return root;
		}

		private class BlockEntityHierarchicalModel extends HierarchicalModel<Entity> {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			}

			public void setupBlockEntityAnim(CannonBlockEntity blockEntity, float ageInTicks) {
				animator.root().getAllParts().forEach(ModelPart::resetPose);
				animator.animate(blockEntity.fireAnimation, CannonAnimations.fire, ageInTicks, 1f);
			}
		}
	}
}