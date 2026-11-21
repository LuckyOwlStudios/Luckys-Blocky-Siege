package net.luckystudios.entity.custom.cannonball.types.explosive_barrel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luckystudios.init.ModBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

// Renders the keg as the explosive barrel block itself, the same way TntRenderer renders primed TNT
public class ExplosiveKegRenderer extends EntityRenderer<ExplosiveKeg> {
    private final BlockRenderDispatcher blockRenderer;

    public ExplosiveKegRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(ExplosiveKeg entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);

        // Boat/minecart-style wobble that gets stronger with every hit
        float wobble = entity.getWobbleTicks() - partialTicks;
        if (wobble > 0.0F) {
            float strength = entity.getHitCount() * 8.0F;
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(wobble) * wobble * strength / 10.0F * entity.getWobbleDirection()));
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        BlockState state = ModBlocks.EXPLOSIVE_BARREL.get().defaultBlockState();
        // Blink white like primed TNT
        boolean flash = entity.tickCount / 5 % 2 == 0;
        TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, state, poseStack, buffer, packedLight, flash);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ExplosiveKeg entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
