package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class DreamFlameParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    protected DreamFlameParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd, sprites.first());
        this.sprites = sprites;
        this.friction = 0.96F;
        this.xd = xd * 0.01D;
        this.yd = yd * 0.01D;
        this.zd = zd * 0.01D;
        this.quadSize *= 1.25F;
        this.lifetime = 8 + this.random.nextInt(4);
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void tick() {
        super.tick();
        this.setSprite(this.sprites.get(this.random));
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
            return new DreamFlameParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
