package net.luckystudios.entity.custom.turrets.ballista;

import net.luckystudios.BlockySiege;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BallistaRenderer extends MobRenderer<Ballista, BallistaGolemModel<Ballista>> {

    private static final ResourceLocation BALLISTA_TEXTURE = BlockySiege.id("textures/entity/ballista/ballista.png");

    public BallistaRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new BallistaGolemModel<>(pContext.bakeLayer(BallistaGolemModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Ballista ballista) {
        return BALLISTA_TEXTURE;
    }
}
