package net.luckystudios.entity.custom.cannon_ball.types.spreading.fire_bomb;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBallRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FireBombRenderer extends AbstractCannonBallRenderer<FireBomb> {

    public static final ResourceLocation FIRE_BOMB_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/projectile/fire_bomb.png");

    public FireBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FireBomb entity) {
        return FIRE_BOMB_LOCATION;
    }
}
