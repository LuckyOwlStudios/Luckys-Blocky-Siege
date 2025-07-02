package net.luckystudios.blocks.custom.iron_gate;

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
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class IronGateRenderer implements BlockEntityRenderer<IronGateBlockEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/gate/iron_gate.png");
	private final ModelPart root;
	private final ModelPart gate;

	public IronGateRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart modelpart = context.bakeLayer(ModModelLayers.IRON_GATE);
		this.root = modelpart.getChild("root");
		this.gate = this.root.getChild("gate");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 36).addBox(-24.0F, -60.0F, -8.0F, 4.0F, 60.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(0, 36).mirror().addBox(20.0F, -60.0F, -8.0F, 4.0F, 60.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 0).addBox(-24.0F, -80.0F, -8.0F, 48.0F, 20.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition gate = root.addOrReplaceChild("gate", CubeListBuilder.create().texOffs(120, 62).addBox(-1.0F, -64.0F, -1.0F, 2.0F, 64.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(120, 62).addBox(-7.0F, -64.0F, -1.0F, 2.0F, 64.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(120, 62).addBox(-13.0F, -64.0F, -1.0F, 2.0F, 64.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(120, 62).addBox(-19.0F, -64.0F, -1.0F, 2.0F, 64.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(120, 62).addBox(-25.0F, -64.0F, -1.0F, 2.0F, 64.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(120, 62).addBox(-31.0F, -64.0F, -1.0F, 2.0F, 64.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(120, 59).addBox(-31.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.25F))
				.texOffs(120, 59).addBox(-25.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.25F))
				.texOffs(120, 59).addBox(-19.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.25F))
				.texOffs(120, 59).addBox(-13.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.25F))
				.texOffs(120, 59).addBox(-7.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.25F))
				.texOffs(120, 59).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.25F))
				.texOffs(0, 124).addBox(-35.0F, -6.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 124).addBox(-35.0F, -12.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 124).addBox(-35.0F, -18.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 124).addBox(-35.0F, -24.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 124).addBox(-35.0F, -30.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 124).addBox(-35.0F, -36.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 124).addBox(-35.0F, -42.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 124).addBox(-35.0F, -48.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 124).addBox(-35.0F, -54.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 124).addBox(-35.0F, -60.0F, -1.0F, 40.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(15.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void render(@NotNull IronGateBlockEntity ironGateBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(TEXTURE_LOCATION));

		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F)); // Flip model upright

		// Render full model from the root
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay);

		poseStack.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(IronGateBlockEntity blockEntity) {
		return new AABB(blockEntity.getBlockPos()).inflate(1, 4, 1);
	}

	@Override
	public int getViewDistance() {
		return 128;
	}
}