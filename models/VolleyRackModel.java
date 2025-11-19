// Made with Blockbench 5.0.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class VolleyRackModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "volleyrackmodel"), "main");
	private final ModelPart base;
	private final ModelPart yaw;
	private final ModelPart barrel;
	private final ModelPart fuse;
	private final ModelPart fuse2;
	private final ModelPart fuse3;
	private final ModelPart fuse4;
	private final ModelPart fuse5;
	private final ModelPart fuse6;
	private final ModelPart fuse7;
	private final ModelPart fuse8;

	public VolleyRackModel(ModelPart root) {
		this.base = root.getChild("base");
		this.yaw = this.base.getChild("yaw");
		this.barrel = this.yaw.getChild("barrel");
		this.fuse = this.barrel.getChild("fuse");
		this.fuse2 = this.barrel.getChild("fuse2");
		this.fuse3 = this.barrel.getChild("fuse3");
		this.fuse4 = this.barrel.getChild("fuse4");
		this.fuse5 = this.barrel.getChild("fuse5");
		this.fuse6 = this.barrel.getChild("fuse6");
		this.fuse7 = this.barrel.getChild("fuse7");
		this.fuse8 = this.barrel.getChild("fuse8");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 28).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition yaw = base.addOrReplaceChild("yaw", CubeListBuilder.create().texOffs(48, 0).addBox(4.0F, -10.0F, -4.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(48, 0).mirror().addBox(-6.0F, -10.0F, -4.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 24).mirror().addBox(-9.0F, -10.0F, -1.0F, 18.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition barrel = yaw.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -4.0F, -8.0F, 16.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 0.0F));

		PartDefinition fuse = barrel.addOrReplaceChild("fuse", CubeListBuilder.create(), PartPose.offset(6.0F, -2.0F, 9.5F));

		PartDefinition fuse_r1 = fuse.addOrReplaceChild("fuse_r1", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition fuse_r2 = fuse.addOrReplaceChild("fuse_r2", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition fuse2 = barrel.addOrReplaceChild("fuse2", CubeListBuilder.create(), PartPose.offset(2.0F, -2.0F, 9.5F));

		PartDefinition fuse_r3 = fuse2.addOrReplaceChild("fuse_r3", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition fuse_r4 = fuse2.addOrReplaceChild("fuse_r4", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition fuse3 = barrel.addOrReplaceChild("fuse3", CubeListBuilder.create(), PartPose.offset(-2.0F, -2.0F, 9.5F));

		PartDefinition fuse_r5 = fuse3.addOrReplaceChild("fuse_r5", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition fuse_r6 = fuse3.addOrReplaceChild("fuse_r6", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition fuse4 = barrel.addOrReplaceChild("fuse4", CubeListBuilder.create(), PartPose.offset(-6.0F, -2.0F, 9.5F));

		PartDefinition fuse_r7 = fuse4.addOrReplaceChild("fuse_r7", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition fuse_r8 = fuse4.addOrReplaceChild("fuse_r8", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition fuse5 = barrel.addOrReplaceChild("fuse5", CubeListBuilder.create(), PartPose.offset(-6.0F, 2.0F, 9.5F));

		PartDefinition fuse_r9 = fuse5.addOrReplaceChild("fuse_r9", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition fuse_r10 = fuse5.addOrReplaceChild("fuse_r10", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition fuse6 = barrel.addOrReplaceChild("fuse6", CubeListBuilder.create(), PartPose.offset(6.0F, 2.0F, 9.5F));

		PartDefinition fuse_r11 = fuse6.addOrReplaceChild("fuse_r11", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition fuse_r12 = fuse6.addOrReplaceChild("fuse_r12", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition fuse7 = barrel.addOrReplaceChild("fuse7", CubeListBuilder.create(), PartPose.offset(2.0F, 2.0F, 9.5F));

		PartDefinition fuse_r13 = fuse7.addOrReplaceChild("fuse_r13", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition fuse_r14 = fuse7.addOrReplaceChild("fuse_r14", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition fuse8 = barrel.addOrReplaceChild("fuse8", CubeListBuilder.create(), PartPose.offset(-2.0F, 2.0F, 9.5F));

		PartDefinition fuse_r15 = fuse8.addOrReplaceChild("fuse_r15", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition fuse_r16 = fuse8.addOrReplaceChild("fuse_r16", CubeListBuilder.create().texOffs(0, -2).addBox(0.0F, -0.5F, -1.5F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}