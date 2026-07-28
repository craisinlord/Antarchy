package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class DreamFlameParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected DreamFlameParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.friction = 0.96F;
        this.xd = xd * 0.01D;
        this.yd = yd * 0.01D;
        this.zd = zd * 0.01D;
        this.quadSize *= 1.25F;
        this.lifetime = 8 + this.random.nextInt(4);
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.pickSprite(this.sprites);
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
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new DreamFlameParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
