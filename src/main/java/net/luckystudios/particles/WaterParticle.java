package net.luckystudios.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class WaterParticle extends TextureSheetParticle {

    private final SpriteSet sprites;
    protected WaterParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet,
                                 double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = spriteSet;
        this.gravity = 0.0F;
        this.friction = 0.999F;

        int color = BiomeColors.getAverageWaterColor(level, new BlockPos((int) x, (int) y, (int) z));

        // Extract RGB components and normalize to 0.0-1.0 range
        this.rCol = ((color >> 16) & 0xFF) / 255.0F;  // Red component
        this.gCol = ((color >> 8) & 0xFF) / 255.0F;   // Green component
        this.bCol = (color & 0xFF) / 255.0F;          // Blue component
        this.alpha = 0.5F;  // Set a reasonable alpha value

        // Optional random nudge
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.lifetime = (int)(4 / (Math.random() * 0.8 + 0.2));
        this.setSpriteFromAge(spriteSet);
        this.quadSize = 0.25F;
        this.hasPhysics = true;
        this.gravity = 0.5F;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel,
                                       double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new WaterParticle(clientLevel, pX, pY, pZ, this.spriteSet, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}
