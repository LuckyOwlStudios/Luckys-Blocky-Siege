package net.luckystudios.entity.custom.cannon_ball.explosive_barrel;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBallRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ExplosiveKegRenderer extends AbstractCannonBallRenderer<ExplosiveKeg> {

    public static final ResourceLocation CANNON_BALL_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/projectile/explosive_keg.png");

    public ExplosiveKegRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ExplosiveKeg entity) {
        return CANNON_BALL_LOCATION;
    }
}
