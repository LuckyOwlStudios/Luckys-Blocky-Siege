package net.luckystudios.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ColorParticleOption;
import org.jetbrains.annotations.Nullable;

public class WaterParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected WaterParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet,
                            double xSpeed, double ySpeed, double zSpeed, ColorParticleOption option) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = spriteSet;
        this.gravity = 0.0F;
        this.friction = 0.999F;

        // Use the color from the ColorParticleOption
        this.rCol = option.getRed();
        this.gCol = option.getGreen();
        this.bCol = option.getBlue();
        this.alpha = 0.5F; // ColorParticleOption also provides alpha!

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

    public static class Provider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(ColorParticleOption colorParticleOption, ClientLevel clientLevel,
                                                 double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new WaterParticle(clientLevel, x, y, z, this.spriteSet, xSpeed, ySpeed, zSpeed, colorParticleOption);
        }
    }
}