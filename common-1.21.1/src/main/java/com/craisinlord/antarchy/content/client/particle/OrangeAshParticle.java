package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class OrangeAshParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private OrangeAshParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.friction = 0.98F;
        this.gravity = 0.0025F;
        this.speedUpWhenYMotionIsBlocked = false;
        this.hasPhysics = false;
        this.xd = xd * 0.08D + (this.random.nextDouble() - 0.5D) * 0.01D;
        this.yd = yd * 0.02D - 0.01D - this.random.nextDouble() * 0.01D;
        this.zd = zd * 0.08D + (this.random.nextDouble() - 0.5D) * 0.01D;
        this.quadSize *= 0.75F + this.random.nextFloat() * 0.5F;
        this.lifetime = 40 + this.random.nextInt(28);
        this.setColor(0.96F, 0.46F, 0.12F);
        this.setAlpha(0.72F);
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.pickSprite(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new OrangeAshParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
