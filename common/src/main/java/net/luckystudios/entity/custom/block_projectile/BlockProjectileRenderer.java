package net.luckystudios.entity.custom.block_projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** Renders the fired block, tumbling end over end through the air. */
public class BlockProjectileRenderer extends EntityRenderer<BlockProjectile> {

    private final BlockRenderDispatcher blockRenderer;

    public BlockProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.25F;
    }

    @Override
    public void render(BlockProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState state = entity.getBlockState();
        float age = entity.tickCount + partialTicks;

        poseStack.pushPose();
        // Pivot around the middle of the block while flipping and spinning
        poseStack.translate(0.0F, 0.25F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(age * 28.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 17.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(age * 9.0F));
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        this.blockRenderer.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BlockProjectile entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
