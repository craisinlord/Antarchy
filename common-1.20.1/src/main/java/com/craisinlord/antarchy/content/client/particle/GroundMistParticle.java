package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class GroundMistParticle extends TextureSheetParticle {
    private final float peakAlpha;
    private final float baseSize;
    private final float spin;
    private final float bobPhase;

    private GroundMistParticle(ClientLevel level, double x, double y, double z, double windX, double peakAlpha, double windZ, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.setSprite(sprites.get(this.random));
        this.lifetime = 240 + this.random.nextInt(160);
        this.baseSize = 1.6F + this.random.nextFloat() * 1.6F;
        this.quadSize = this.baseSize;
        this.peakAlpha = (float) peakAlpha;
        this.spin = (this.random.nextFloat() - 0.5F) * 0.004F;
        this.bobPhase = this.random.nextFloat() * Mth.TWO_PI;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        float tint = 0.9F + this.random.nextFloat() * 0.08F;
        this.setColor(tint, tint * 0.97F, 1.0F);
        this.setAlpha(0.0F);
        this.xd = windX * (0.7D + this.random.nextDouble() * 0.6D);
        this.yd = 0.0D;
        this.zd = windZ * (0.7D + this.random.nextDouble() * 0.6D);
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = this.age / (float) this.lifetime;
        this.roll += this.spin;
        this.quadSize = this.baseSize * (1.0F + progress * 0.35F);
        double bob = Mth.sin(this.age * 0.03F + this.bobPhase) * 0.0025D;
        this.move(this.xd, bob, this.zd);

        float fadeIn = Mth.clamp(progress / 0.25F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((1.0F - progress) / 0.35F, 0.0F, 1.0F);
        float fade = Math.min(fadeIn, fadeOut);
        this.setAlpha(this.peakAlpha * fade * fade * (3.0F - 2.0F * fade));
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
            return new GroundMistParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
