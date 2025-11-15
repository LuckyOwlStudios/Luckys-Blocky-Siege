package net.luckystudios.entity.custom.cannonball.types.normal;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.cannonball.AbstractCannonBallRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CannonBallRenderer extends AbstractCannonBallRenderer<CannonBall> {

    public static final ResourceLocation CANNON_BALL_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/projectile/cannonball.png");

    public CannonBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CannonBall entity) {
        return CANNON_BALL_LOCATION;
    }
}
