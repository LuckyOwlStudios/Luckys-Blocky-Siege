package net.luckystudios.entity.custom.potion_blob;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.water_drop.WaterBlobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PotionBlobRenderer extends WaterBlobRenderer<PotionBlob> {

    public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(BlockySiege.MOD_ID, "textures/entity/projectile/potion_bubble.png");


    public PotionBlobRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PotionBlob entity) {
        return TEXTURE_LOCATION;
    }

    @Override
    public int getColor(PotionBlob entity) {
        return entity.getColor();
    }
}
