package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class LucidBoltImpactParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private LucidBoltImpactParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites, float baseScale, int minLifetime, int randomLifetime) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;
        this.friction = 0.88F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = baseScale * (0.85F + this.random.nextFloat() * 0.3F);
        this.lifetime = minLifetime + this.random.nextInt(randomLifetime);
        this.setColor(0.94F, 0.95F, 1.0F);
        this.setAlpha(0.95F);
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.pickSprite(this.sprites);
        float life = (float) this.age / (float) this.lifetime;
        this.setAlpha(Mth.clamp(1.0F - life, 0.0F, 1.0F) * 0.95F);
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class SmallProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SmallProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new LucidBoltImpactParticle(level, x, y, z, xd, yd, zd, this.sprites, 0.1F, 8, 4);
        }
    }

    public static final class LargeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public LargeProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new LucidBoltImpactParticle(level, x, y, z, xd, yd, zd, this.sprites, 0.22F, 10, 4);
        }
    }
}
