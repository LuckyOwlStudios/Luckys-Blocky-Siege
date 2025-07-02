package net.luckystudios.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class BottleCapParticle extends TextureSheetParticle {

    private final float rotSpeed;

    protected BottleCapParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet,
                                double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.gravity = 0.75F;
        this.friction = 0.999F;

        // Optional random nudge
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.lifetime = (int)(16.0F / (Math.random() * 0.8 + 0.2));
        this.setSpriteFromAge(spriteSet);
        this.rotSpeed = ((float)Math.random() - 0.5F) * 0.1F;
        this.roll = (float)Math.random() * ((float)Math.PI * 2F);
        this.quadSize = 0.625F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age++ < this.lifetime) {
            this.oRoll = this.roll;
            this.roll += (float)Math.PI * this.rotSpeed * 2.0F;
            if (this.onGround) {
                this.oRoll = this.roll = 0.0F;
            }
        }
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
            return new BottleCapParticle(clientLevel, pX, pY, pZ, this.spriteSet, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}
