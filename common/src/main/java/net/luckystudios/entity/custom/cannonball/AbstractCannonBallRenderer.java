package net.luckystudios.entity.custom.cannonball;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.luckystudios.BlockySiege;
import net.luckystudios.util.ModModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

// Copied from the ArrowRenderer class in Minecraft
// Also used WindChargeRenderer as a reference
// This basically handles the rotation of the model
public abstract class AbstractCannonBallRenderer<T extends AbstractCannonBall> extends EntityRenderer<T> {

    public static final ResourceLocation TRAIL_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/projectile/wind_trail.png");


    private final CannonBallModel model;
    private final TrailModel trailModel;

    public AbstractCannonBallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CannonBallModel(context.bakeLayer(ModModelLayers.CANNON_BALL));
        this.trailModel = new TrailModel(context.bakeLayer(ModModelLayers.TRAIL));
        this.shadowRadius = 0.25F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        boolean ball = entity.usesBallPhysics();
        if (ball) {
            // Bouncing balls keep their hitbox on the ground, so draw the ball in the middle of it
            poseStack.translate(0.0F, entity.getBbHeight() * 0.5F, 0.0F);
        }

        // Boat/minecart-style wobble that gets stronger with every hit
        float wobble = entity.getWobbleTicks() - partialTicks;
        if (wobble > 0.0F) {
            float strength = entity.getHitCount() * 8.0F;
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(wobble) * wobble * strength / 10.0F * entity.getWobbleDirection()));
        }

        // Interpolated yaw and pitch, same as Fabric
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F;
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F;

        // Render the model: turned by its spin once it has bounced, otherwise facing the way it flies
        poseStack.pushPose();
        Quaternionf spin = ball ? entity.getSpin(partialTicks) : null;
        if (spin != null) {
            poseStack.mulPose(spin);
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        }
        float f = (float)entity.tickCount + partialTicks;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        if (!entity.isStuck && entity.showsTrail()) {
            if (ball) {
                // Point the trail straight back along the way the ball is going, which can change at every bounce
                Vec3 motion = entity.getDeltaMovement();
                yaw = (float)(Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG) - 90.0F;
                pitch = (float)(Mth.atan2(motion.y, motion.horizontalDistance()) * Mth.RAD_TO_DEG) + 90.0F;
            }
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
            VertexConsumer vertexConsumer2 = buffer.getBuffer(RenderType.breezeWind(TRAIL_LOCATION, 0.0F, this.xOffset(f, entity) % 1.0F));
            trailModel.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
            trailModel.renderToBuffer(poseStack, vertexConsumer2, packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    // Move the wind trail texture by how fast the entity is moving
    protected float xOffset(float tickCount, AbstractCannonBall abstractCannonBall) {
        double speed = abstractCannonBall.getDeltaMovement().length() * 0.05;
        return -tickCount * (float)speed;
    }
}
