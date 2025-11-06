package net.luckystudios.entity.custom.cannon_ball.types.spreading.frost_bomb;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBallRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FrostBombRenderer extends AbstractCannonBallRenderer<FrostBomb> {

    public static final ResourceLocation CANNON_BALL_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/projectile/frost_bomb.png");

    public FrostBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FrostBomb entity) {
        return CANNON_BALL_LOCATION;
    }
}
