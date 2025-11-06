// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class SpewerCannonModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "spewercannonmodel"), "main");
	private final ModelPart yaw;
	private final ModelPart cannon;
	private final ModelPart pitch;
	private final ModelPart barrel;
	private final ModelPart contents;

	public SpewerCannonModel(ModelPart root) {
		this.yaw = root.getChild("yaw");
		this.cannon = this.yaw.getChild("cannon");
		this.pitch = this.cannon.getChild("pitch");
		this.barrel = this.pitch.getChild("barrel");
		this.contents = this.yaw.getChild("contents");
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
		.texOffs(38, 78).addBox(-7.0F, -18.0F, -3.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(64, 44).addBox(-5.0F, -18.0F, -1.0F, 10.0F, 14.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition pitch = cannon.addOrReplaceChild("pitch", CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -9.0F, 4.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 18).addBox(-9.0F, -4.0F, -4.0F, 18.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition barrel = pitch.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(18, 86).addBox(-3.0F, -3.0F, -15.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 85).addBox(-4.0F, -4.0F, -16.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition contents = yaw.addOrReplaceChild("contents", CubeListBuilder.create().texOffs(38, 0).addBox(-5.0F, -7.0F, -1.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		yaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}