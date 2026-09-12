package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class UndertrialParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final boolean animated;

    private UndertrialParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                               SpriteSet sprites, boolean animated) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.animated = animated;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.gravity = 0.0F;
        this.friction = 0.96F;
        this.hasPhysics = false;
        this.quadSize = animated ? 0.16F + this.random.nextFloat() * 0.08F : 0.2F + this.random.nextFloat() * 0.1F;
        this.lifetime = animated ? 10 : 24;
        this.setAlpha(0.9F);
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.animated) {
            this.setSprite(this.sprites.get(this.age, this.lifetime));
        }
        this.setAlpha(0.9F * (1.0F - (float) this.age / (float) this.lifetime));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class DetectionProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public DetectionProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new UndertrialParticle(level, x, y, z, xd, yd, zd, this.sprites, true);
        }
    }

    public static final class OmenProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public OmenProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new UndertrialParticle(level, x, y, z, xd, yd, zd, this.sprites, false);
        }
    }
}
