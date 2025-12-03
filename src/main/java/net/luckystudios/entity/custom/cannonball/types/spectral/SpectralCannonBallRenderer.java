package net.luckystudios.entity.custom.cannonball.types.spectral;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.cannonball.AbstractCannonBallRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SpectralCannonBallRenderer extends AbstractCannonBallRenderer<SpectralCannonBall> {

    public static final ResourceLocation SPECTRAL_CANNON_BALL_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/projectile/spectral_cannonball.png");

    public SpectralCannonBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SpectralCannonBall entity) {
        return SPECTRAL_CANNON_BALL_LOCATION;
    }
}
