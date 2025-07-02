// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class MultiCannonModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "multicannonmodel"), "main");
	private final ModelPart yaw;
	private final ModelPart pitch;
	private final ModelPart barrel;
	private final ModelPart barrel_0;
	private final ModelPart barrel_1;
	private final ModelPart barrel_2;
	private final ModelPart barrel_3;

	public MultiCannonModel(ModelPart root) {
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
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		yaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}