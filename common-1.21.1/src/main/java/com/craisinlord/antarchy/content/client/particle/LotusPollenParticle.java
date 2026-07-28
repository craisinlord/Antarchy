package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class LotusPollenParticle extends TextureSheetParticle {
    private static final int LIFETIME = 140;

    private LotusPollenParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.setSprite(sprites.get(this.random));
        this.gravity = 0.0F;
        this.friction = 0.98F;
        this.lifetime = LIFETIME + this.random.nextInt(60);
        this.quadSize = 0.06F + this.random.nextFloat() * 0.03F;
        this.xd = xd + (this.random.nextDouble() - 0.5D) * 0.01D;
        this.yd = yd;
        this.zd = zd + (this.random.nextDouble() - 0.5D) * 0.01D;
        this.setColor(1.0F, 0.85F, 0.2F);
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.alpha = 1.0F - (float) this.age / (float) this.lifetime;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.zd *= this.friction;
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
            return new LotusPollenParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
