package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class BugSprayParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private BugSprayParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.setSprite(sprites.get(level.random));
        this.lifetime = 14 + this.random.nextInt(8);
        this.quadSize = 0.16F + this.random.nextFloat() * 0.14F;
        this.friction = 0.88F;
        this.gravity = -0.01F;
        this.alpha = 0.9F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSprite(this.sprites.get(Math.min(this.age, this.lifetime - 1), this.lifetime));
            this.alpha = 0.9F * (1.0F - (float) this.age / this.lifetime);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new BugSprayParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
