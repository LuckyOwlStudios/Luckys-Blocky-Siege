package net.luckystudios.entity.custom.cannon_ball.wind_bomb;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBallRenderer;
import net.luckystudios.entity.custom.cannon_ball.normal.CannonBall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class WindBombRenderer extends AbstractCannonBallRenderer<net.luckystudios.entity.custom.cannon_ball.normal.CannonBall> {

    public static final ResourceLocation CANNON_BALL_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/projectile/wind_bomb.png");

    public WindBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CannonBall entity) {
        return CANNON_BALL_LOCATION;
    }
}
