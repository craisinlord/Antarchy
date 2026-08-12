package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class HypnoticGasParticle extends BaseAshSmokeParticle {
    private final float fadeOutStartingPoint;

    private HypnoticGasParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            float scale,
            boolean flipped,
            SpriteSet sprites
    ) {
        super(level, x, y, z, 0.1F, 0.1F, 0.1F, xd, yd, zd, scale, sprites, 0.3F, 5, -0.02F, true);
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        if (flipped) {
            this.roll = Mth.PI;
            this.oRoll = Mth.PI;
        }
        this.lifetime = (int) (6.0D / (this.random.nextFloat() * 0.5D + 0.5D) * scale);
        this.fadeOutStartingPoint = this.lifetime / 2.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > this.fadeOutStartingPoint) {
            float agePastFadeStart = this.age - this.fadeOutStartingPoint;
            this.setAlpha((this.lifetime - agePastFadeStart) / this.lifetime);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final boolean flipped;

        public Provider(SpriteSet sprites) {
            this(sprites, false);
        }

        public Provider(SpriteSet sprites, boolean flipped) {
            this.sprites = sprites;
            this.flipped = flipped;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new HypnoticGasParticle(level, x, y, z, xd, yd, zd, 3.0F, this.flipped, this.sprites);
        }
    }
}
