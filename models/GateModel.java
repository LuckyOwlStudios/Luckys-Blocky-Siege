// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class GateModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "gatemodel"), "main");
	private final ModelPart root;
	private final ModelPart gate;

	public GateModel(ModelPart root) {
		this.root = root.getChild("root");
		this.gate = this.root.getChild("gate");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 20).addBox(-24.0F, -60.0F, -8.0F, 4.0F, 60.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).mirror().addBox(20.0F, -60.0F, -8.0F, 4.0F, 60.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).addBox(-24.0F, -64.0F, -8.0F, 48.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

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
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}