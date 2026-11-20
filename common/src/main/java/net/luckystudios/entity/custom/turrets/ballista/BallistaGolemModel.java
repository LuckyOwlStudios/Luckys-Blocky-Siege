package net.luckystudios.entity.custom.turrets.ballista;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luckystudios.BlockySiege;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class BallistaGolemModel<T extends BallistaEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(BlockySiege.id("ballista"), "main");
    private final ModelPart root;
    private final ModelPart ballista;
    private final ModelPart turret;
    private final ModelPart ammo;
    private final ModelPart nose;
    private final ModelPart banners;
    private final ModelPart string_left;
    private final ModelPart string_right;

    public BallistaGolemModel(ModelPart root) {
        this.root = root;
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
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, int i2) {
        ballista.render(poseStack, vertexConsumer, i, i1, i2);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T t, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        setStringPose(t);
        setTurretRotations(netHeadYaw, headPitch);
    }

    // Strings are anchored at the limb tips (x = +-12) and run back towards the stock.
    // Relaxed they angle in slightly; fully drawn they meet in the middle, further back.
    private static final float STRING_LENGTH = 20.0F;
    private static final float LIMB_OFFSET = 12.0F;
    private static final float RELAXED_ANGLE = 0.333F;
    private static final float RELAXED_DEPTH = STRING_LENGTH * Mth.cos(RELAXED_ANGLE);
    // Negative: the strings are drawn towards the front of the turret.
    private static final float DRAW_DISTANCE = -6.0F;

    private void setStringPose(BallistaEntity entity) {
        boolean visible = entity.isCrossbowInstalled();
        this.string_left.visible = visible;
        this.string_right.visible = visible;
        if (!visible) {
            return;
        }
        float progress = Mth.clamp(entity.getChargeProgress(), 0.0F, 1.0F);
        float depth = RELAXED_DEPTH + DRAW_DISTANCE * progress;
        float drawnAngle = (float) Math.atan2(LIMB_OFFSET, RELAXED_DEPTH + DRAW_DISTANCE);
        float angle = Mth.lerp(progress, RELAXED_ANGLE, drawnAngle);
        float length = depth / Mth.cos(angle);
        float scale = length / STRING_LENGTH;
        this.string_left.yRot = -angle;
        this.string_right.yRot = angle;
        this.string_left.zScale = scale;
        this.string_right.zScale = scale;
    }

    private void setTurretRotations(float netHeadYaw, float headPitch) {

        headPitch = Math.clamp(headPitch, -60, 30);

        // Convert to radians
        float yawRad = (float) Math.toRadians(netHeadYaw);
        float pitchRad = (float) Math.toRadians(headPitch);

        // Apply yaw rotation to the turret (horizontal rotation)
        this.ballista.yRot = yawRad;

        // Apply pitch rotation to the turret (vertical elevation)
        this.turret.xRot = pitchRad;
    }
}
