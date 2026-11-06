package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class RemembranceMoteParticle extends TextureSheetParticle {
    private static final int FULL_BRIGHT = 0xF000F0;

    private final SpriteSet sprites;
    private final float swayPhase;

    private RemembranceMoteParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.xd = 0.0D;
        this.yd = 0.012D + this.random.nextDouble() * 0.012D;
        this.zd = 0.0D;
        this.quadSize = 0.07F + this.random.nextFloat() * 0.04F;
        this.lifetime = 40 + this.random.nextInt(30);
        this.swayPhase = this.random.nextFloat() * Mth.TWO_PI;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        this.setSpriteFromAge(this.sprites);
        float wave = this.age * 0.15F + this.swayPhase;
        this.xd = Mth.sin(wave) * 0.006F;
        this.zd = Mth.cos(wave * 0.8F) * 0.006F;
        this.alpha = Math.min(1.0F, (this.lifetime - this.age) / 15.0F);
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULL_BRIGHT;
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
            return new RemembranceMoteParticle(level, x, y, z, this.sprites);
        }
    }
}
