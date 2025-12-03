// Made with Blockbench 5.0.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class SloopModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "sloopmodel"), "main");
	private final ModelPart ship;
	private final ModelPart post;
	private final ModelPart post_yaw;
	private final ModelPart sail;
	private final ModelPart bottom;
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
		this.post = this.ship.getChild("post");
		this.post_yaw = this.post.getChild("post_yaw");
		this.sail = this.post_yaw.getChild("sail");
		this.bottom = this.ship.getChild("bottom");
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

		PartDefinition post = ship.addOrReplaceChild("post", CubeListBuilder.create().texOffs(92, 0).addBox(-3.0F, -69.0F, -3.0F, 6.0F, 64.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition post_yaw = post.addOrReplaceChild("post_yaw", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = post_yaw.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(116, 0).mirror().addBox(-1.0F, -24.0F, -1.0F, 2.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -32.0F, -4.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r2 = post_yaw.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(116, 0).mirror().addBox(-1.0F, -24.0F, -1.0F, 2.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -66.0F, -4.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r3 = post_yaw.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(40, 94).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -66.0F, -4.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r4 = post_yaw.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(40, 94).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -32.0F, -4.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition sail = post_yaw.addOrReplaceChild("sail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r5 = sail.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(158, 0).mirror().addBox(-24.0F, -16.0F, 0.0F, 48.0F, 32.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -49.0F, -5.0F, 0.0F, 0.0F, 3.1416F));

		PartDefinition cube_r6 = sail.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(158, 0).addBox(-24.0F, -16.0F, 0.0F, 48.0F, 32.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -49.0F, -5.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition bottom = ship.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-16.0F, -40.0F, -1.0F, 32.0F, 80.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(69, 99).addBox(-4.0F, -4.0F, 2.0F, 8.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition left = ship.addOrReplaceChild("left", CubeListBuilder.create().texOffs(70, 0).addBox(-10.0F, -40.0F, -11.0F, 8.0F, 80.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(92, 70).addBox(-13.0F, 13.0F, -11.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(92, 70).addBox(-13.0F, -19.0F, -11.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition right = ship.addOrReplaceChild("right", CubeListBuilder.create().texOffs(70, 0).mirror().addBox(2.0F, -40.0F, -11.0F, 8.0F, 80.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, -1.5708F, 0.0F, -1.5708F));

		PartDefinition cube_r7 = right.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(92, 70).addBox(-5.0F, 9.0F, -1.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 28.0F, -10.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition cube_r8 = right.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(92, 70).addBox(-5.0F, 9.0F, -1.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -4.0F, -10.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition front = ship.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 97).addBox(-16.0F, -11.0F, -43.0F, 32.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(102, 74).addBox(-4.0F, -11.0F, -46.0F, 8.0F, 11.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(78, 83).addBox(-4.0F, -19.0F, -51.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition back = ship.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 83).addBox(-18.0F, -14.0F, 40.0F, 36.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition paddle_left = ship.addOrReplaceChild("paddle_left", CubeListBuilder.create().texOffs(0, 108).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
		.texOffs(0, 108).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, -12.0F, 16.0F, 2.1642F, 0.8727F, 2.8798F));

		PartDefinition paddle_left2 = ship.addOrReplaceChild("paddle_left2", CubeListBuilder.create().texOffs(0, 108).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
		.texOffs(0, 108).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, -12.0F, -16.0F, 2.1642F, 0.8727F, 2.8798F));

		PartDefinition paddle_right = ship.addOrReplaceChild("paddle_right", CubeListBuilder.create().texOffs(0, 108).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
		.texOffs(0, 108).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.0F, -12.0F, -16.0F, 2.1642F, -0.8727F, -2.8798F));

		PartDefinition paddle_right2 = ship.addOrReplaceChild("paddle_right2", CubeListBuilder.create().texOffs(0, 108).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
		.texOffs(0, 108).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.0F, -12.0F, 16.0F, 2.1642F, -0.8727F, -2.8798F));

		PartDefinition water = partdefinition.addOrReplaceChild("water", CubeListBuilder.create().texOffs(-24, 168).addBox(-15.0F, -11.0F, -40.0F, 30.0F, 8.0F, 80.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		ship.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		water.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}