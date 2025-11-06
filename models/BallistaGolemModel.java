// Made with Blockbench 5.0.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class BallistaGolemModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "ballistagolemmodel"), "main");
	private final ModelPart ballista;
	private final ModelPart turret;
	private final ModelPart ammo;
	private final ModelPart nose;
	private final ModelPart banners;
	private final ModelPart string_left;
	private final ModelPart string_right;

	public BallistaGolemModel(ModelPart root) {
		this.ballista = root.getChild("ballista");
		this.turret = this.ballista.getChild("turret");
		this.ammo = this.turret.getChild("ammo");
		this.nose = this.turret.getChild("nose");
		this.banners = this.turret.getChild("banners");
		this.string_left = this.turret.getChild("string_left");
		this.string_right = this.turret.getChild("string_right");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ballista = partdefinition.addOrReplaceChild("ballista", CubeListBuilder.create().texOffs(60, 0).addBox(-9.0F, -6.0F, -3.0F, 18.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition turret = ballista.addOrReplaceChild("turret", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -13.0F, -16.0F, 14.0F, 14.0F, 32.0F, new CubeDeformation(0.0F))
		.texOffs(0, 46).addBox(-15.0F, -9.0F, -14.0F, 30.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition ammo = turret.addOrReplaceChild("ammo", CubeListBuilder.create().texOffs(52, 46).addBox(-3.0F, -12.0F, -4.0F, 6.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

		PartDefinition nose = turret.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, -16.0F));

		PartDefinition banners = turret.addOrReplaceChild("banners", CubeListBuilder.create().texOffs(56, 56).addBox(-14.0F, -6.0F, -14.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(56, 56).mirror().addBox(8.0F, -6.0F, -14.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition string_left = turret.addOrReplaceChild("string_left", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(12.0F, -6.0F, -10.0F));

		PartDefinition cube_r1 = string_left.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition string_right = turret.addOrReplaceChild("string_right", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0F, -6.0F, -10.0F));

		PartDefinition cube_r2 = string_right.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		ballista.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}